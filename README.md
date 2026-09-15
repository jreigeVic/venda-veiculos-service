# venda-veiculos-service

Autor: [jreigeVic](https://github.com/jreigeVic)

## O que é o projeto

Serviço de **venda de veículos** de uma plataforma de revenda de veículos automotores,
desenvolvido como Trabalho Substitutivo de Tech Challenge (Fase 4, Pós Tech SOAT).

## Para que serve

É responsável pela **listagem de veículos** (à venda, vendidos, ou por status via
`GET /veiculos?status=`) e por **efetuar a venda** de um veículo, incluindo o webhook que recebe a
confirmação de pagamento da processadora externa. Tem banco de dados próprio, isolado do banco do
software principal, e é desenhado para suportar picos repentinos de chamadas sem depender de o
software principal estar no ar — mantém uma projeção local dos dados de veículo, sincronizada via
HTTP.

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
- Swagger UI interativo: `GET /swagger-ui/index.html` (com a app rodando localmente), gerado a
  partir dos controllers reais via springdoc.
- [`postman/`](postman/) — coleção Postman para testar as rotas manualmente.

## Como foi implementado

Spring Boot 4.1.1, Java 26, Gradle (Kotlin DSL), Spring Data JPA, PostgreSQL, Spring Boot
Actuator/Micrometer (observabilidade), springdoc-openapi (Swagger UI). O endpoint interno de
sincronização é protegido por token compartilhado (`X-Internal-Token`), não pelo mecanismo de
login de usuário. Segue **Arquitetura Hexagonal** (Ports & Adapters): Domain sem dependência de
Spring/JPA/HTTP — com as regras de negócio (disponibilidade e reserva de veículo, transições de
status de pagamento, idempotência do webhook) encapsuladas nos próprios agregados `VeiculoProjecao`
e `Venda` — Application conhecendo apenas Ports, Adapters isolando a tecnologia concreta; ver
detalhamento em [`docs/lld.md`](docs/lld.md). Testes automatizados cobrindo Domain, Application e
Adapters, com cobertura acima de 97% (mínimo exigido: 80%). CI/CD validado de ponta a ponta via
GitHub Actions.

## Estrutura do projeto

```
src/main/java/com/soat/vendaveiculos/
├── veiculo/
│   ├── domain/          # VeiculoProjecao (disponibilidade/reserva), StatusVeiculo, EstadoConservacao
│   ├── application/     # Use Cases (criar/atualizar/listar projeção) e Ports
│   └── adapter/         # in/web (controllers interno + listagens) e out/persistence (JPA)
├── venda/
│   ├── domain/          # Venda (transições de pagamento), CpfValidador, StatusPagamento
│   ├── application/     # Use Cases (efetuar venda, processar webhook) e Ports
│   └── adapter/         # in/web (controllers venda + webhook) e out/persistence (JPA)
├── auditoria/           # AuditoriaPort + adapter de persistência do log de sucesso/erro
├── web/                 # GlobalExceptionHandler (tratamento de erro transversal)
└── config/              # segurança do endpoint interno, etc.
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
