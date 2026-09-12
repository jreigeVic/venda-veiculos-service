# Modelagem de domínio

O enunciado avisa explicitamente: *"nem todos os campos necessários estão descritos acima, por
isso a modelagem é fundamental para entender como resolver o problema"*. Este documento detalha
as entidades usadas nos dois serviços, incluindo os campos inferidos (não citados no enunciado) e
a justificativa de cada um. Decisões de modelagem mais amplas (onde os dados vivem, quem é dono de
quê) estão em [`decisoes-pendentes.md`](./decisoes-pendentes.md), itens 2 a 4.

## Software principal — entidade `Veiculo` (fonte da verdade dos dados cadastrais)

| Campo         | Tipo      | Origem                | Observação                                   |
|---------------|-----------|------------------------|-----------------------------------------------|
| `id`          | UUID      | inferido               | identificador estável, usado também no serviço de venda |
| `marca`       | String    | enunciado              |                                                |
| `modelo`      | String    | enunciado              |                                                |
| `ano`         | Integer   | enunciado              |                                                |
| `cor`         | String    | enunciado              |                                                |
| `preco`       | BigDecimal| enunciado              | usado para ordenação nas listagens do serviço de venda |
| `estadoConservacao` | Enum | inferido           | `NOVO` (0 KM) \| `SEMINOVO` \| `USADO` — revenda lida com veículos em vários estados |
| `criadoEm`    | Instant   | inferido               | auditoria                                     |
| `atualizadoEm`| Instant   | inferido               | auditoria; toda edição atualiza este campo    |

Cadastro e edição não têm campo de "status de venda" no software principal — esse dado pertence
ao serviço de venda, que é quem sabe se o veículo está disponível, reservado ou vendido (ver
decisão nº 2).

## Serviço de venda — projeção `VeiculoProjecao` (cópia sincronizada via HTTP)

| Campo     | Tipo       | Origem                          | Observação                                        |
|-----------|------------|----------------------------------|-----------------------------------------------------|
| `id`      | UUID       | igual ao `id` do software principal | chave de correlação entre os dois serviços      |
| `marca`   | String     | sincronizado                     |                                                      |
| `modelo`  | String     | sincronizado                     |                                                      |
| `ano`     | Integer    | sincronizado                     |                                                      |
| `cor`     | String     | sincronizado                     |                                                      |
| `preco`   | BigDecimal | sincronizado                     | usado para `ORDER BY preco ASC` nas duas listagens  |
| `estadoConservacao` | Enum | sincronizado             | `NOVO` \| `SEMINOVO` \| `USADO`                     |
| `status`  | Enum       | inferido — fonte da verdade aqui | `DISPONIVEL` \| `RESERVADO` \| `VENDIDO`            |

Ciclo de vida do `status`:

```
DISPONIVEL --(POST /vendas)--> RESERVADO --(webhook: APROVADO)--> VENDIDO
                                    │
                                    └──(webhook: CANCELADO)--> DISPONIVEL
```

Um veículo só pode ser vendido (`POST /vendas`) se estiver `DISPONIVEL` — evita duas vendas
simultâneas do mesmo veículo (concorrência tratada com lock otimista / verificação de versão no
banco de dados).

## Serviço de venda — entidade `Venda`

| Campo             | Tipo      | Origem     | Observação                                          |
|-------------------|-----------|------------|-------------------------------------------------------|
| `id`              | UUID      | inferido   |                                                        |
| `veiculoId`       | UUID      | inferido   | referência à `VeiculoProjecao`                        |
| `cpfComprador`    | String    | enunciado  | validado por formato e dígito verificador             |
| `dataVenda`       | LocalDate | enunciado  | data em que a venda foi efetuada (criação do registro) |
| `codigoPagamento` | UUID      | inferido   | gerado na criação da venda; usado pelo webhook         |
| `statusPagamento` | Enum      | inferido   | `PENDENTE` \| `APROVADO` \| `CANCELADO`                |
| `criadoEm`        | Instant   | inferido   | auditoria                                              |
| `atualizadoEm`    | Instant   | inferido   | auditoria; atualizado quando o webhook chega           |

## Software principal — entidade `EventoSincronizacao` (padrão Outbox)

Garante que o cadastro/edição nunca falha por causa do serviço de venda estar indisponível, e que
a sincronização é reprocessada automaticamente quando ele volta (ver decisão nº 2 em
`decisoes-pendentes.md`).

| Campo         | Tipo    | Observação                                                        |
|---------------|---------|--------------------------------------------------------------------|
| `id`          | UUID    |                                                                      |
| `veiculoId`   | UUID    | referência ao `Veiculo`                                             |
| `tipoEvento`  | Enum    | `VEICULO_CRIADO` \| `VEICULO_ATUALIZADO`                            |
| `payload`     | JSON    | corpo a ser enviado para `/interno/veiculos`                        |
| `status`      | Enum    | `PENDENTE` \| `ENTREGUE` \| `FALHOU_DEFINITIVAMENTE`                |
| `tentativas`  | Integer | número de tentativas de entrega já feitas                          |
| `proximaTentativaEm` | Instant | usado pelo job de reenvio (backoff exponencial)             |
| `criadoEm`    | Instant |                                                                      |
| `atualizadoEm`| Instant |                                                                      |

Gravado na **mesma transação** do cadastro/edição do veículo. Um job agendado varre eventos
`PENDENTE` com `proximaTentativaEm <= agora` e tenta entregá-los; sucesso (`2xx`) marca `ENTREGUE`,
falha incrementa `tentativas` e recalcula `proximaTentativaEm` com backoff.

## Log de auditoria (nos dois serviços)

Tabela de auditoria para rastrear sucesso e erro nos fluxos sensíveis: cadastro/edição de veículo
(software principal) e venda/pagamento (serviço de venda). Uma tabela por serviço, mesmo formato.

| Campo         | Tipo    | Observação                                                            |
|---------------|---------|--------------------------------------------------------------------------|
| `id`          | UUID    |                                                                          |
| `servico`     | String  | nome do serviço que gerou o log                                        |
| `operacao`    | String  | ex.: `CADASTRAR_VEICULO`, `EDITAR_VEICULO`, `EFETUAR_VENDA`, `WEBHOOK_PAGAMENTO` |
| `entidadeId`  | UUID    | id do veículo ou da venda envolvida                                    |
| `resultado`   | Enum    | `SUCESSO` \| `ERRO`                                                     |
| `detalhe`     | String  | mensagem de erro (quando `ERRO`) ou resumo do que mudou (quando `SUCESSO`) |
| `criadoEm`    | Instant |                                                                          |

Alimentada por um aspecto/listener em torno dos casos de uso (não espalhada manualmente em cada
controller), para garantir que toda operação relevante — sucesso ou falha — gere um registro.

## Autenticação entre serviços

`POST/PUT /interno/veiculos` (chamado pelo software principal para o serviço de venda) usa um
**token compartilhado** simples (`X-Internal-Token`) em vez do mecanismo de login de usuário do
Spring Security — ver decisão nº 10. Os demais endpoints de negócio voltados a usuário continuam
protegidos normalmente.

## Por que a projeção local (e não uma consulta síncrona a cada listagem)

Resumo da justificativa completa (ver decisão nº 2): o próprio enunciado justifica o isolamento do
serviço de venda por precisar suportar picos de tráfego nas listagens e compras. Se cada listagem
dependesse de uma chamada HTTP síncrona ao software principal para buscar marca/modelo/preço, o
isolamento não cumpriria esse objetivo. A projeção local aceita consistência eventual (uma edição
de preço pode levar um instante para refletir na listagem) em troca de isolamento real de carga.
