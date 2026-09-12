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

## Componentes — Software principal

```mermaid
flowchart TB
    subgraph "Software principal"
        API["Controller REST<br/>/veiculos"]
        SVC["VeiculoService"]
        REPO[("VeiculoRepository<br/>PostgreSQL")]
        OUTBOX[("EventoSincronizacaoRepository<br/>(Outbox)")]
        JOB["Job de reenvio<br/>(scheduled, backoff)"]
        SEC["Spring Security<br/>(usuário) / token interno"]
        LOG[("LogAuditoria")]
    end

    HTTP_CLIENT["Serviço de venda<br/>/interno/veiculos"]

    API --> SEC
    API --> SVC
    SVC --> REPO
    SVC -->|grava evento na mesma transação| OUTBOX
    SVC --> LOG
    JOB --> OUTBOX
    JOB -->|POST/PUT com X-Internal-Token| HTTP_CLIENT
```

## Componentes — Serviço de venda de veículos

```mermaid
flowchart TB
    subgraph "Serviço de venda de veículos"
        INT["Controller interno<br/>/interno/veiculos<br/>(token compartilhado)"]
        LIST["Controller REST<br/>/veiculos/a-venda, /veiculos/vendidos"]
        VENDA_C["Controller REST<br/>/vendas"]
        HOOK["Controller REST<br/>/pagamentos/webhook"]
        SVC2["VendaService / VeiculoProjecaoService"]
        REPO2[("VeiculoProjecaoRepository<br/>PostgreSQL isolado")]
        REPO3[("VendaRepository<br/>PostgreSQL isolado")]
        LOG2[("LogAuditoria")]
    end

    MOCK["Mock processadora de pagamento<br/>(handler estilo Lambda, execução local)"]

    INT --> SVC2 --> REPO2
    LIST --> REPO2
    VENDA_C --> SVC2 --> REPO3
    HOOK --> SVC2
    SVC2 --> LOG2
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
