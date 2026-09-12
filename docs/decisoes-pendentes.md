# Decisões pendentes de confirmação

Este documento reúne toda decisão de projeto que não estava explicitamente definida no enunciado
(`Fase 4 - Trabalho Reposição Tech Challenge SOAT.pdf`) e que exigiu uma escolha para o trabalho
avançar. Revisado e respondido pelo usuário em 2026-09-12. Os itens abaixo já refletem as decisões
finais — mantido como histórico de por que cada escolha foi feita, não mais como perguntas em
aberto (exceto onde marcado).

Status: `[x]` confirmado/resolvido pelo usuário · `[~]` alterado pelo usuário · `[ ]` ainda aberto

---

## 1. Divisão dos dois repositórios

**Decisão final:** este repositório (`jreigeVic/posVendaAuto`) é o **"software principal"**.
Um segundo repositório, **`venda-veiculos-service`**, será criado em
`github.com/jreigeVic/venda-veiculos-service` (público) e terá seu conteúdo local publicado nele.
Os dois repositórios seguem **desacoplados desde o início** — sem monorepo, sem histórico
compartilhado.

**Autorização confirmada pelo usuário:** criação do repositório via `gh repo create` e o primeiro
`git push`, desde que tudo seja feito em nome do usuário e sem nenhuma marca d'água de IA (regra
já registrada em memória permanente, item [[feedback-no-watermark]]).

`[x]` Confirmado

---

## 2. Onde vivem os dados do veículo (sincronização entre serviços)

**Decisão final:** mantida a projeção local no serviço de venda, sincronizada via HTTP a partir do
software principal a cada cadastro/edição (ver `docs/arquitetura.md` e `docs/contratos-api.md`).

**Resiliência do sync — escolha concreta:** em vez de introduzir Redis (mais um container, mais
uma peça para cobrir com testes, e que não resolve nada que um outbox não resolva), o software
principal implementa o **padrão Outbox**: cada cadastro/edição grava um evento de sincronização
pendente na própria tabela/base do software principal, dentro da mesma transação do
cadastro/edição. Um job agendado tenta entregar os eventos pendentes ao serviço de venda com
backoff, marcando como entregue ao receber `2xx`. Isso garante que o cadastro nunca falha por
causa do serviço de venda estar fora do ar, e que a sincronização é automaticamente reprocessada
quando ele volta — que é exatamente a resiliência que a sugestão de "cache" buscava, sem adicionar
um componente de infraestrutura novo. Se, ao usar, isso não for suficiente, dá para evoluir para
um message broker depois.

`[x]` Confirmado (com a escolha técnica de outbox em vez de cache dedicado — sinalizar se preferir
Redis/fila de mensagens de fato)

---

## 3. Webhook de pagamento + mock da processadora

**Decisão final:** webhook (`POST /pagamentos/webhook`) permanece no serviço de venda, contrato
idempotente como descrito em `docs/contratos-api.md`.

**Mock da processadora de pagamento:** como não há credenciais de nuvem disponíveis agora (ver
item 7), o mock será implementado como uma função no formato de **AWS Lambda** (handler Java,
mesmo formato de invocação `RequestHandler`), mas **executada localmente** (via `sam local
invoke` ou um runner simples), simulando a chamada ao webhook com `APROVADO`/`CANCELADO` alguns
segundos após a criação da venda. Quando houver credenciais AWS reais, o mesmo artefato pode ser
publicado como Lambda de verdade sem alterações — só muda o gatilho.

`[x]` Confirmado

---

## 4. Campos e ciclo de vida do veículo — estado de conservação

**Decisão final:** adicionado o campo `estadoConservacao` ao `Veiculo` (software principal) e
propagado na projeção do serviço de venda, com os valores: `NOVO` (0 KM), `SEMINOVO`, `USADO`.
Ver `docs/modelagem.md`, já atualizado.

`[x]` Confirmado

---

## 5. Stack técnica e observabilidade

**Decisão final:** stack mantida (Spring Boot 4.1.1, Gradle Kotlin DSL, Lombok, PostgreSQL).
Observabilidade: habilitado Spring Boot Actuator + Micrometer (grátis, já vem com o starter) nos
dois serviços, expondo métricas em formato compatível com múltiplos backends. O **New Relic foi
citado como opção futura, não uma exigência imediata** ("precisamos olhar para... gosto do New
Relic como opção") — por isso não foi acoplado um agente que não pode ser testado sem uma licença.
Quando houver uma conta/license key do New Relic, basta adicionar o agente Java e apontar o
Micrometer para o registry da New Relic; a base (Actuator/Micrometer) já fica pronta para isso.

`[x]` Confirmado

---

## 6. Toolchain Java 26

**Decisão final:** mantido Java 26 nos dois repositórios, por decisão explícita do usuário. Risco
de indisponibilidade nos runners do GitHub Actions será validado na prática na primeira execução
do workflow; plano B (Java 21 LTS) documentado e pronto se necessário.

`[x]` Confirmado

---

## 7. Infraestrutura de deploy

**Decisão final:** sem credenciais de nuvem disponíveis no momento. Deploy demonstrado
**localmente com `kind`** (Kubernetes-in-Docker), aplicando os mesmos manifests (`Deployment` +
`Service`) que seriam usados em um cluster gerenciado real (ex.: AWS EKS) — a estrutura dos
manifests é a mesma, só muda onde são aplicados. Isso cumpre o pedido de "mockar localmente uma
estrutura do EKS" sem custo e sem depender de credenciais.

`[x]` Confirmado

---

## 8. Nome de autoria nos READMEs

**Decisão final:** mantido o crédito como está (`jreigeVic`, com link para o perfil do GitHub).

`[x]` Confirmado

---

## 9. Gate de cobertura de 80%

**Decisão final:** o gate do JaCoCo que quebra o build abaixo de 80% (`jacocoTestCoverageVerification`
amarrado à task `check`) passa a valer **nos dois repositórios**, não só no serviço de venda.

`[x]` Confirmado

---

## 10. Autenticação entre os serviços (decisão nova, necessária para o fluxo funcionar)

Com `spring-boot-starter-security` no software principal, qualquer endpoint fica protegido por
padrão assim que os controllers forem criados — inclusive `POST/PUT /interno/veiculos`, que quem
chama é o **serviço de venda**, não um usuário autenticado via login.

**Decisão adotada:** os endpoints internos (`/interno/**`) usam um **token compartilhado simples**
(header `X-Internal-Token`, valor vindo de variável de ambiente/secret, igual nos dois serviços) em
vez de ficarem protegidos pelo mesmo mecanismo de autenticação de usuário. Os endpoints de negócio
voltados a usuário (cadastro/edição de veículo) continuam protegidos por Spring Security normal.

`[x]` Confirmado (registrar aqui se preferir OAuth2/mTLS entre serviços em vez de token
compartilhado — token simples foi escolhido por ser suficiente para o escopo do trabalho)

---

## 11. Entregáveis adicionais solicitados nesta rodada

Pedido do usuário — status após a implementação:

- [x] Diagramas Mermaid: fluxograma da aplicação e diagramas de sequência por fluxo de negócio.
- [x] HLD (High-Level Design) e LLD (Low-Level Design) completos — em `docs/hld.md` e `docs/lld.md`.
- [x] `openapi.yaml` (Swagger) em cada repositório.
- [x] Coleção Postman por repositório, cobrindo todas as rotas.
- [x] Tabela(s) de log/auditoria para sucesso e erro nos fluxos de cadastro de veículo e de venda
      (`LogAuditoria`, alimentada pelos services e pelo `GlobalExceptionHandler` de cada serviço).
- [x] READMEs atualizados, com link cruzado entre as documentações, explicando o que cada serviço
      faz, para que serve, sua estrutura e como rodar/testar.
- [x] Manter a documentação atualizada — este item permanece como prática contínua a cada mudança.

`[x]` Concluído nesta rodada.

---

## 12. Implementação e validação end-to-end real (nesta rodada)

Além da documentação, foi implementado o código funcional dos dois serviços (entidades,
controllers, services, exceptions, segurança, outbox, webhook, mock de pagamento) com testes
automatizados (~96–97% de cobertura de linha em ambos, acima dos 80% exigidos) e o pipeline de
CI/CD completo foi **executado de verdade** no GitHub Actions, não apenas escrito:

- CI (`ci.yml`, em PR): build + testes + gate de cobertura de 80% — passou nos dois repositórios,
  confirmando que **Java 26 está disponível** via `actions/setup-java` + Temurin (resolve a dúvida
  do item 6 — não foi necessário usar o plano B de Java 21).
- CD (`cd.yml`, no merge para `main`): build da imagem Docker, push para `ghcr.io`, criação de um
  cluster `kind` efêmero no próprio runner, aplicação dos manifests Kubernetes e espera do rollout
  — **executado com sucesso de ponta a ponta nos dois repositórios**, com o Postgres subindo no
  cluster e a aplicação conectando nele e respondendo `/actuator/health`.
- Bug real encontrado e corrigido pelo próprio pipeline: o `venda-veiculos-service` subia na porta
  8080 (padrão do Spring Boot) enquanto o `Deployment`/`Service`/probes esperavam 8081, causando
  `CrashLoopBackOff` — sem `server.port` configurado. Corrigido adicionando
  `server.port=${SERVER_PORT:8081}` em `application.properties`. Identificado através dos logs de
  diagnóstico adicionados ao próprio workflow de CD.

Todas as mudanças (incluindo essa correção) foram feitas via Pull Request e merge, com CI
passando antes do merge — nenhum commit foi feito direto na `main` de nenhum dos dois repositórios.

**O que ainda depende de você:** o vídeo de demonstração ponta-a-ponta (cadastro → sincronização →
compra → webhook de pagamento → listagens) e a montagem do PDF final de entrega com os links dos
dois repositórios — isso é execução manual/gravação, fora do que posso fazer.

`[x]` Concluído nesta rodada.
