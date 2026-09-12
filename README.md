# venda-veiculos-service

Autor: [jreigeVic](https://github.com/jreigeVic)

## O que é o projeto

Serviço de **venda de veículos** de uma plataforma de revenda de veículos automotores,
desenvolvido como Trabalho Substitutivo de Tech Challenge (Fase 4, Pós Tech SOAT).

## Para que serve

É responsável pela **listagem de veículos à venda e vendidos** (ordenadas por preço crescente) e
por **efetuar a venda** de um veículo, incluindo o webhook que recebe a confirmação de pagamento
da processadora externa. Tem banco de dados próprio, isolado do banco do software principal, e é
desenhado para suportar picos repentinos de chamadas sem depender de o software principal estar
no ar — mantém uma projeção local dos dados de veículo, sincronizada via HTTP.

O repositório do software principal (cadastro/edição de veículo) é o
[`posVendaAuto`](https://github.com/jreigeVic/posVendaAuto). Os dois serviços se comunicam apenas
por HTTP — nenhum acesso direto a banco entre eles.

## Documentação

- [`docs/arquitetura.md`](docs/arquitetura.md) — visão geral da solução e fluxo ponta a ponta.
- [`docs/hld.md`](docs/hld.md) — High-Level Design: contexto, componentes, infraestrutura de deploy.
- [`docs/lld.md`](docs/lld.md) — Low-Level Design: classes, entidades, banco de dados, sequências.
- [`docs/modelagem.md`](docs/modelagem.md) — entidades, campos e ciclo de vida do veículo/venda.
- [`docs/contratos-api.md`](docs/contratos-api.md) — contratos REST dos dois serviços.
- [`docs/decisoes-pendentes.md`](docs/decisoes-pendentes.md) — decisões de projeto tomadas para
  preencher lacunas do enunciado, com a justificativa de cada uma.
- [`openapi.yaml`](openapi.yaml) — especificação Swagger/OpenAPI desta API.
- [`postman/`](postman/) — coleção Postman para testar as rotas manualmente.

## Como foi implementado

Spring Boot 4.1.1, Java 26, Gradle (Kotlin DSL), Spring Data JPA, PostgreSQL, Spring Boot
Actuator/Micrometer (observabilidade). O endpoint interno de sincronização é protegido por token
compartilhado (`X-Internal-Token`), não pelo mecanismo de login de usuário. Estado atual: em
desenvolvimento — a modelagem e os contratos de API estão definidos e documentados em `docs/`; a
implementação de código (entidades, controllers, persistência, webhook, testes com cobertura
mínima de 80% e pipeline de CI/CD) está em andamento.

## Estrutura do projeto

```
src/main/java/com/soat/vendaveiculos/
├── veiculo/           # projeção do veículo (endpoint interno + listagens)
├── venda/              # efetuar venda + webhook de pagamento
├── auditoria/          # log de sucesso/erro das operações
└── config/             # segurança do endpoint interno, etc.
docs/                    # arquitetura, HLD/LLD, modelagem, contratos de API, decisões
openapi.yaml             # especificação Swagger/OpenAPI
postman/                 # coleção Postman
k8s/                     # manifests Deployment + Service (deploy local via kind)
mock-pagamento/          # mock da processadora de pagamento (handler estilo Lambda, execução local)
```

## Como usar localmente

Pré-requisitos: JDK 26 e Docker (para o banco de dados via `compose.yaml`).

```bash
./gradlew bootRun
```

No Windows, use `gradlew.bat bootRun`.

## Como testar

```bash
./gradlew test      # roda os testes
./gradlew check     # roda os testes e falha se a cobertura ficar abaixo de 80%
```

Relatório de cobertura (JaCoCo) gerado em `build/reports/jacoco/test/html/index.html`. O build
falha (`./gradlew check`) se a cobertura ficar abaixo de 80%, conforme exigido para este serviço.

No Windows, use `gradlew.bat test` / `gradlew.bat check`.
