# LLD — Low-Level Design

Detalhamento de classes, entidades, banco de dados e sequências de cada fluxo de negócio. Este é
o **desenho que o código deve seguir** — a implementação ainda está em andamento (ver
`decisoes-pendentes.md`, item 11). Para a visão de componentes e infraestrutura, ver
[`hld.md`](./hld.md).

## Fluxograma da aplicação (ciclo de vida do veículo/venda)

```mermaid
stateDiagram-v2
    [*] --> DISPONIVEL: Veículo cadastrado (software principal) e sincronizado
    DISPONIVEL --> RESERVADO: POST /vendas (compra iniciada)
    RESERVADO --> VENDIDO: Webhook: pagamento APROVADO
    RESERVADO --> DISPONIVEL: Webhook: pagamento CANCELADO
    VENDIDO --> [*]
```

## Diagrama de classes — Software principal

```mermaid
classDiagram
    class Veiculo {
        +UUID id
        +String marca
        +String modelo
        +Integer ano
        +String cor
        +BigDecimal preco
        +EstadoConservacao estadoConservacao
        +Instant criadoEm
        +Instant atualizadoEm
    }

    class EstadoConservacao {
        <<enumeration>>
        NOVO
        SEMINOVO
        USADO
    }

    class EventoSincronizacao {
        +UUID id
        +UUID veiculoId
        +TipoEvento tipoEvento
        +String payload
        +StatusEvento status
        +Integer tentativas
        +Instant proximaTentativaEm
    }

    class TipoEvento {
        <<enumeration>>
        VEICULO_CRIADO
        VEICULO_ATUALIZADO
    }

    class StatusEvento {
        <<enumeration>>
        PENDENTE
        ENTREGUE
        FALHOU_DEFINITIVAMENTE
    }

    class LogAuditoria {
        +UUID id
        +String servico
        +String operacao
        +UUID entidadeId
        +Resultado resultado
        +String detalhe
        +Instant criadoEm
    }

    class Resultado {
        <<enumeration>>
        SUCESSO
        ERRO
    }

    class VeiculoController {
        +cadastrar(VeiculoRequest) VeiculoResponse
        +editar(UUID, VeiculoRequest) VeiculoResponse
    }

    class VeiculoService {
        +cadastrar(VeiculoRequest) Veiculo
        +editar(UUID, VeiculoRequest) Veiculo
    }

    class SincronizacaoJob {
        +reenviarPendentes() void
    }

    class VendaVeiculosClient {
        +sincronizar(EventoSincronizacao) void
    }

    Veiculo --> EstadoConservacao
    EventoSincronizacao --> TipoEvento
    EventoSincronizacao --> StatusEvento
    LogAuditoria --> Resultado
    VeiculoController --> VeiculoService
    VeiculoService --> Veiculo
    VeiculoService --> EventoSincronizacao : grava na mesma transação
    VeiculoService --> LogAuditoria
    SincronizacaoJob --> EventoSincronizacao
    SincronizacaoJob --> VendaVeiculosClient
```

## Diagrama de classes — Serviço de venda de veículos

```mermaid
classDiagram
    class VeiculoProjecao {
        +UUID id
        +String marca
        +String modelo
        +Integer ano
        +String cor
        +BigDecimal preco
        +EstadoConservacao estadoConservacao
        +StatusVeiculo status
    }

    class StatusVeiculo {
        <<enumeration>>
        DISPONIVEL
        RESERVADO
        VENDIDO
    }

    class Venda {
        +UUID id
        +UUID veiculoId
        +String cpfComprador
        +LocalDate dataVenda
        +UUID codigoPagamento
        +StatusPagamento statusPagamento
        +Instant criadoEm
        +Instant atualizadoEm
    }

    class StatusPagamento {
        <<enumeration>>
        PENDENTE
        APROVADO
        CANCELADO
    }

    class VeiculoInternoController {
        +criar(VeiculoSyncRequest) void
        +atualizar(UUID, VeiculoSyncRequest) void
    }

    class VeiculoListagemController {
        +listarAVenda() List~VeiculoResponse~
        +listarVendidos() List~VeiculoResponse~
    }

    class VendaController {
        +efetuarVenda(VendaRequest) VendaResponse
    }

    class WebhookPagamentoController {
        +receber(WebhookRequest) void
    }

    class VendaService {
        +efetuarVenda(VendaRequest) Venda
        +processarWebhook(WebhookRequest) void
    }

    class LogAuditoria {
        +UUID id
        +String servico
        +String operacao
        +UUID entidadeId
        +Resultado resultado
        +String detalhe
        +Instant criadoEm
    }

    VeiculoProjecao --> StatusVeiculo
    VeiculoProjecao --> EstadoConservacao
    Venda --> StatusPagamento
    VeiculoInternoController --> VeiculoProjecao
    VeiculoListagemController --> VeiculoProjecao
    VendaController --> VendaService
    WebhookPagamentoController --> VendaService
    VendaService --> Venda
    VendaService --> VeiculoProjecao
    VendaService --> LogAuditoria
```

## Modelo de dados (ER) — Software principal

```mermaid
erDiagram
    VEICULO {
        uuid id PK
        string marca
        string modelo
        int ano
        string cor
        decimal preco
        string estado_conservacao
        timestamp criado_em
        timestamp atualizado_em
    }
    EVENTO_SINCRONIZACAO {
        uuid id PK
        uuid veiculo_id FK
        string tipo_evento
        text payload
        string status
        int tentativas
        timestamp proxima_tentativa_em
    }
    LOG_AUDITORIA {
        uuid id PK
        string servico
        string operacao
        uuid entidade_id
        string resultado
        string detalhe
        timestamp criado_em
    }

    VEICULO ||--o{ EVENTO_SINCRONIZACAO : gera
```

## Modelo de dados (ER) — Serviço de venda de veículos

```mermaid
erDiagram
    VEICULO_PROJECAO {
        uuid id PK
        string marca
        string modelo
        int ano
        string cor
        decimal preco
        string estado_conservacao
        string status
    }
    VENDA {
        uuid id PK
        uuid veiculo_id FK
        string cpf_comprador
        date data_venda
        uuid codigo_pagamento
        string status_pagamento
        timestamp criado_em
        timestamp atualizado_em
    }
    LOG_AUDITORIA {
        uuid id PK
        string servico
        string operacao
        uuid entidade_id
        string resultado
        string detalhe
        timestamp criado_em
    }

    VEICULO_PROJECAO ||--o{ VENDA : "é vendido em"
```

## Sequência — Cadastrar veículo (com sincronização resiliente)

```mermaid
sequenceDiagram
    actor Operador
    participant API as Software principal (API)
    participant DB1 as PostgreSQL (principal)
    participant Job as Job de reenvio
    participant Venda as Serviço de venda (API)
    participant DB2 as PostgreSQL (venda)

    Operador->>API: POST /veiculos
    API->>DB1: INSERT Veiculo + INSERT EventoSincronizacao (mesma transação)
    API-->>Operador: 201 Created

    loop até entregar
        Job->>DB1: buscar eventos PENDENTE
        Job->>Venda: POST /interno/veiculos (X-Internal-Token)
        alt sucesso
            Venda->>DB2: INSERT VeiculoProjecao (status=DISPONIVEL)
            Venda-->>Job: 201 Created
            Job->>DB1: marcar evento ENTREGUE
        else falha (serviço de venda fora do ar)
            Job->>DB1: incrementar tentativas + backoff
        end
    end
```

## Sequência — Efetuar venda e confirmação de pagamento

```mermaid
sequenceDiagram
    actor Comprador
    participant Venda as Serviço de venda (API)
    participant DB2 as PostgreSQL (venda)
    participant Mock as Mock processadora de pagamento

    Comprador->>Venda: POST /vendas {veiculoId, cpfComprador, dataVenda}
    Venda->>DB2: veículo está DISPONIVEL?
    alt disponível
        Venda->>DB2: INSERT Venda (statusPagamento=PENDENTE) + UPDATE status=RESERVADO
        Venda-->>Comprador: 201 Created {codigoPagamento}
        Mock->>Venda: POST /pagamentos/webhook {codigoPagamento, status=APROVADO}
        Venda->>DB2: UPDATE Venda.statusPagamento=APROVADO, VeiculoProjecao.status=VENDIDO
        Venda-->>Mock: 200 OK
    else indisponível
        Venda-->>Comprador: 409 Conflict
    end
```

## Sequência — Webhook chamado novamente (idempotência)

```mermaid
sequenceDiagram
    participant Mock as Processadora de pagamento
    participant Venda as Serviço de venda (API)
    participant DB2 as PostgreSQL (venda)

    Mock->>Venda: POST /pagamentos/webhook {codigoPagamento, status=APROVADO}
    Venda->>DB2: Venda.statusPagamento já é APROVADO?
    alt já está em estado final
        Venda-->>Mock: 200 OK (nenhuma alteração — idempotente)
    else ainda PENDENTE
        Venda->>DB2: aplica a transição normalmente
        Venda-->>Mock: 200 OK
    end
```

## Sequência — Listagem de veículos à venda

```mermaid
sequenceDiagram
    actor Comprador
    participant Venda as Serviço de venda (API)
    participant DB2 as PostgreSQL (venda)

    Comprador->>Venda: GET /veiculos/a-venda
    Venda->>DB2: SELECT * FROM veiculo_projecao WHERE status='DISPONIVEL' ORDER BY preco ASC
    DB2-->>Venda: lista ordenada
    Venda-->>Comprador: 200 OK [...]
```
