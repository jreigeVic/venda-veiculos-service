# HLD — High-Level Design

Visão de alto nível da solução: contexto, componentes por serviço, integração e infraestrutura de
deploy. Para o detalhamento de classes, entidades e sequências, ver [`lld.md`](./lld.md). Para o
porquê de cada escolha, ver [`decisoes-pendentes.md`](./decisoes-pendentes.md).

## Diagrama de contexto

```mermaid
flowchart TB
    operador(["Operador da revenda"]) -->|"cadastra/edita veículo (HTTPS)"| principal
    comprador(["Comprador"]) -->|"lista e compra veículo (HTTPS)"| venda

    subgraph plataforma ["Plataforma de revenda de veículos"]
        principal["Software principal<br/>cadastro e edição de veículos"]
        venda["Serviço de venda de veículos<br/>listagens, compra e pagamento"]
        principal -->|"sincroniza projeção do veículo (HTTP interno)"| venda
    end

    pagamento(["Processadora de pagamento (externa)"]) -->|"notifica status via webhook"| venda
```

Internamente, os dois serviços seguem **Arquitetura Hexagonal** (Ports & Adapters): o Domain não
conhece Spring/JPA/HTTP; a Application (Use Cases) só conhece Ports; Adapters (web, scheduler,
persistência JPA, cliente HTTP) isolam a tecnologia concreta.

## Componentes — Software principal

```mermaid
flowchart TB
    subgraph "Software principal"
        API["Adapter in/web<br/>VeiculoController<br/>/veiculos"]
        UC["Application (Use Cases)<br/>CadastrarVeiculoService<br/>EditarVeiculoService"]
        DOM["Domain<br/>Veiculo"]
        REPO[("Adapter out/persistence<br/>VeiculoPersistenceAdapter<br/>PostgreSQL")]
        SYNCSVC["Application<br/>SincronizacaoService<br/>(SincronizacaoEventoPort)"]
        OUTBOX[("Adapter out/persistence<br/>EventoSincronizacaoPersistenceAdapter<br/>(Outbox)")]
        JOBADAPTER["Adapter in/scheduler<br/>SincronizacaoJob"]
        JOBSVC["Application<br/>ReenviarEventosPendentesService<br/>(backoff, retry)"]
        SEC["Spring Security<br/>(usuário) / token interno"]
        AUDPORT["Application Port<br/>AuditoriaPort"]
        LOG[("Adapter out/persistence<br/>AuditoriaPersistenceAdapter")]
    end

    HTTP_ADAPTER["Adapter out/http<br/>HttpVendaVeiculosAdapter"]
    HTTP_CLIENT["Serviço de venda<br/>/interno/veiculos"]

    API --> SEC
    API --> UC
    UC --> DOM
    UC --> REPO
    UC -->|grava evento na mesma transação| SYNCSVC
    SYNCSVC --> OUTBOX
    UC --> AUDPORT --> LOG
    JOBADAPTER --> JOBSVC
    JOBSVC --> OUTBOX
    JOBSVC --> HTTP_ADAPTER
    JOBSVC --> AUDPORT
    HTTP_ADAPTER -->|POST/PUT com X-Internal-Token| HTTP_CLIENT
```

## Componentes — Serviço de venda de veículos

```mermaid
flowchart TB
    subgraph "Serviço de venda de veículos"
        INT["Adapter in/web<br/>VeiculoInternoController<br/>/interno/veiculos<br/>(token compartilhado)"]
        LIST["Adapter in/web<br/>VeiculoListagemController<br/>/veiculos/a-venda, /veiculos/vendidos"]
        VENDA_C["Adapter in/web<br/>VendaController<br/>/vendas"]
        HOOK["Adapter in/web<br/>WebhookPagamentoController<br/>/pagamentos/webhook"]
        UC2["Application (Use Cases)<br/>VeiculoProjecaoService<br/>VendaService"]
        DOM2["Domain<br/>VeiculoProjecao, Venda<br/>(reservar/vender/liberar,<br/>aprovar/cancelar, CpfValidador)"]
        REPO2[("Adapter out/persistence<br/>VeiculoProjecaoPersistenceAdapter<br/>PostgreSQL isolado")]
        REPO3[("Adapter out/persistence<br/>VendaPersistenceAdapter<br/>PostgreSQL isolado")]
        AUDPORT2["Application Port<br/>AuditoriaPort"]
        LOG2[("Adapter out/persistence<br/>AuditoriaPersistenceAdapter")]
    end

    MOCK["Mock processadora de pagamento<br/>(handler estilo Lambda, execução local)"]

    INT --> UC2
    LIST --> UC2
    VENDA_C --> UC2
    HOOK --> UC2
    UC2 --> DOM2
    UC2 --> REPO2
    UC2 --> REPO3
    UC2 --> AUDPORT2 --> LOG2
    MOCK -->|POST /pagamentos/webhook| HOOK
```

## Infraestrutura de deploy

Cada serviço é empacotado como imagem Docker e publicado no GitHub Container Registry
(`ghcr.io`) no merge para `main`. O deploy é demonstrado localmente com **`kind`**
(Kubernetes-in-Docker), aplicando os mesmos manifests (`Deployment` + `Service`) que valeriam em
um cluster gerenciado real — ver decisão nº 7 em `decisoes-pendentes.md`.

```mermaid
flowchart LR
    subgraph "GitHub"
        PR["Pull Request"] -->|build + testes + cobertura >= 80%| CI["CI"]
        MERGE["Merge em main"] --> CD["CD: build imagem + push ghcr.io"]
    end

    CD --> REG[("ghcr.io<br/>registro de imagens")]

    subgraph "Cluster kind (local, mock de EKS)"
        subgraph "Namespace: posvenda"
            DEP1["Deployment<br/>software-principal"]
            SVC1["Service<br/>software-principal"]
            DB1[("PostgreSQL<br/>posvenda_principal_db")]
        end
        subgraph "Namespace: venda-veiculos"
            DEP2["Deployment<br/>venda-veiculos-service"]
            SVC2b["Service<br/>venda-veiculos-service"]
            DB2[("PostgreSQL<br/>venda_veiculos_db")]
        end
    end

    REG --> DEP1
    REG --> DEP2
    SVC1 <-->|HTTP interno| SVC2b
    DEP1 --> DB1
    DEP2 --> DB2
```

## CI/CD por repositório

```mermaid
flowchart LR
    A["Push em branch de feature"] --> B["Abrir Pull Request"]
    B --> C{"CI: build + testes"}
    C -->|falha| B
    C -->|passa + cobertura >= 80%| D["Merge em main"]
    D --> E["CD: build da imagem Docker"]
    E --> F["Push para ghcr.io"]
    F --> G["Deploy: kubectl apply nos manifests k8s"]
```
