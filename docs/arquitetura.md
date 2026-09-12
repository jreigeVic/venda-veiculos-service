# Arquitetura da solução

> Este documento descreve a arquitetura completa da plataforma de revenda de veículos exigida
> pelo Tech Challenge Fase 4 (Pós Tech SOAT). As decisões marcadas como padrão/assumidas estão
> detalhadas, com a justificativa, em [`decisoes-pendentes.md`](./decisoes-pendentes.md).

## Visão geral

A solução é composta por **dois serviços independentes**, cada um em seu próprio repositório,
com seu próprio banco de dados, comunicando-se exclusivamente por HTTP:

```
                    ┌─────────────────────────────┐
                    │   Software principal         │
                    │   (repo: posVendaAuto)       │
                    │                               │
                    │  - Cadastro de veículo        │
                    │  - Edição de veículo           │
                    │  - Banco: posvenda_principal   │
                    └──────────────┬────────────────┘
                                   │ HTTP (sincroniza projeção
                                   │ do veículo a cada cadastro/edição)
                                   ▼
                    ┌─────────────────────────────┐
                    │  Serviço de venda de veículos │
                    │  (repo: venda-veiculos-service)│
                    │                               │
                    │  - Listagem à venda            │
                    │  - Listagem vendidos            │
                    │  - Efetuar venda (compra)       │
                    │  - Webhook de pagamento         │
                    │  - Banco: venda_veiculos_db      │
                    └─────────────────────────────┘
                                   ▲
                                   │ HTTP
                          Entidade processadora
                          de pagamento (externa/simulada)
```

## Por que essa divisão

O enunciado exige que os **endpoints de listagem e compra** fiquem isolados em um serviço próprio,
com banco de dados isolado, justamente para suportar picos repentinos de chamadas sem afetar o
resto do sistema. Isso implica duas coisas:

1. O serviço de venda **não pode depender de uma chamada síncrona ao software principal** em toda
   requisição de listagem ou compra — senão o isolamento não cumpre seu objetivo (um pico ali
   ainda derrubaria o principal).
2. Por isso o serviço de venda mantém uma **projeção própria** dos dados de veículo necessários
   para listar e vender (ver decisão nº 2 em `decisoes-pendentes.md`).

## Serviço 1 — Software principal

Responsabilidades:
- **Cadastrar veículo** (`marca`, `modelo`, `ano`, `cor`, `preço`) — fonte da verdade desses dados.
- **Editar veículo**.
- Após cada cadastro/edição bem-sucedida, propaga a projeção atualizada para o serviço de venda
  via HTTP (`POST`/`PUT /interno/veiculos`).
- Dono do seu próprio banco de dados (`posvenda_principal_db`), independente do banco do serviço
  de venda.

## Serviço 2 — Serviço de venda de veículos

Responsabilidades:
- **Listagem de veículos à venda**, ordenada por preço crescente.
- **Listagem de veículos vendidos**, ordenada por preço crescente.
- **Efetuar a venda** de um veículo (`cpfComprador`, `dataVenda`), gerando um `codigoPagamento`.
- **Webhook de pagamento**: recebe de uma entidade externa (processadora de pagamento) a
  confirmação ou cancelamento do pagamento associado a um `codigoPagamento`.
- Mantém a projeção local dos dados de veículo (sincronizada pelo software principal) e é a fonte
  da verdade para o `status` do veículo (disponível/reservado/vendido).
- Dono do seu próprio banco de dados (`venda_veiculos_db`), fisicamente separado do banco do
  software principal.
- Isolado para suportar aumento repentino de chamadas — ver seção acima.

## Fluxo ponta a ponta (o que o vídeo de demonstração precisa mostrar)

1. Cliente cadastra um veículo no **software principal** (`POST /veiculos`).
2. Software principal sincroniza a projeção com o **serviço de venda** (`POST /interno/veiculos`).
3. Veículo aparece na listagem "à venda" do serviço de venda (`GET /veiculos/a-venda`), com
   `status = DISPONIVEL`.
4. Comprador efetua a compra no serviço de venda (`POST /vendas`), informando `cpf` e o veículo.
   O veículo passa a `status = RESERVADO` e uma `Venda` é criada com `statusPagamento = PENDENTE`
   e um `codigoPagamento` gerado.
5. A processadora de pagamento (simulada) chama o webhook (`POST /pagamentos/webhook`) informando
   `APROVADO` ou `CANCELADO` para aquele `codigoPagamento`.
6. Se `APROVADO`: veículo passa a `status = VENDIDO` e passa a aparecer na listagem de "vendidos"
   (`GET /veiculos/vendidos`). Se `CANCELADO`: veículo volta a `status = DISPONIVEL`.

## Comunicação entre serviços

Toda comunicação entre os dois serviços é feita via **requisições HTTP** (REST/JSON), nunca via
acesso direto a banco de dados do outro serviço. Ver contratos completos em
[`contratos-api.md`](./contratos-api.md).

## CI/CD

Cada repositório tem seu próprio pipeline (GitHub Actions), independente:
- **Pull Request**: build + testes automatizados. No serviço de venda de veículos, o build falha
  se a cobertura de testes ficar abaixo de 80%.
- **Merge na branch principal**: build da imagem Docker e publicação (gatilho automático, sem
  intervenção manual), seguido de deploy dos manifests Kubernetes (`Deployment` + `Service`).

Detalhes de infraestrutura de deploy (onde os manifests são aplicados) estão registrados como
decisão pendente nº 7 em `decisoes-pendentes.md`.

## Modelagem de domínio

Ver [`modelagem.md`](./modelagem.md) para entidades, campos e ciclo de vida (incluindo os campos
inferidos, já que o enunciado avisa que nem todos estão descritos).
