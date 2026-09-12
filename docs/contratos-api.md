# Contratos de API

Convenções: JSON, `Content-Type: application/json`, datas em ISO-8601. Códigos de erro seguem o
padrão HTTP (`400` validação, `404` não encontrado, `409` conflito de estado, `422` regra de
negócio violada).

## Software principal (`posVendaAuto`)

### `POST /veiculos` — cadastrar veículo

Requisição:
```json
{
  "marca": "Fiat",
  "modelo": "Argo",
  "ano": 2022,
  "cor": "Prata",
  "preco": 78900.00
}
```

Resposta `201 Created`:
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "marca": "Fiat",
  "modelo": "Argo",
  "ano": 2022,
  "cor": "Prata",
  "preco": 78900.00,
  "criadoEm": "2026-09-12T10:00:00Z"
}
```

Efeito colateral: dispara `POST /interno/veiculos` no serviço de venda (ver abaixo) com o mesmo
`id`, para criar a projeção com `status = DISPONIVEL`.

### `PUT /veiculos/{id}` — editar veículo

Mesmo corpo de requisição do cadastro. Resposta `200 OK` com o veículo atualizado.

Efeito colateral: dispara `PUT /interno/veiculos/{id}` no serviço de venda para atualizar a
projeção (marca/modelo/ano/cor/preço — nunca o `status`, que pertence ao serviço de venda).

---

## Serviço de venda de veículos (`venda-veiculos-service`)

### `POST /interno/veiculos` — endpoint interno, chamado pelo software principal

Cria a projeção local de um veículo recém-cadastrado.

Requisição:
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "marca": "Fiat",
  "modelo": "Argo",
  "ano": 2022,
  "cor": "Prata",
  "preco": 78900.00
}
```

Resposta `201 Created`. `status` inicial sempre `DISPONIVEL`.

### `PUT /interno/veiculos/{id}` — endpoint interno, chamado pelo software principal

Atualiza marca/modelo/ano/cor/preço da projeção (não altera `status`). Resposta `200 OK`.

### `GET /veiculos/a-venda` — listagem de veículos à venda

Retorna veículos com `status = DISPONIVEL`, ordenados por `preco` crescente.

Resposta `200 OK`:
```json
[
  { "id": "...", "marca": "Fiat", "modelo": "Argo", "ano": 2022, "cor": "Prata", "preco": 78900.00 },
  { "id": "...", "marca": "VW", "modelo": "Nivus", "ano": 2023, "cor": "Branco", "preco": 99900.00 }
]
```

### `GET /veiculos/vendidos` — listagem de veículos vendidos

Retorna veículos com `status = VENDIDO`, ordenados por `preco` crescente. Mesmo formato acima.

### `POST /vendas` — efetuar a venda de um veículo

Requisição:
```json
{
  "veiculoId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "cpfComprador": "12345678901",
  "dataVenda": "2026-09-12"
}
```

Regras: falha com `409 Conflict` se o veículo não estiver `DISPONIVEL`. Falha com `400` se o CPF
for inválido.

Resposta `201 Created`:
```json
{
  "id": "8f14e45f-ceea-467e-8d3f-a3b1e0f2c1a9",
  "veiculoId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "cpfComprador": "12345678901",
  "dataVenda": "2026-09-12",
  "codigoPagamento": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "statusPagamento": "PENDENTE"
}
```

Efeito colateral: veículo passa a `status = RESERVADO`.

### `POST /pagamentos/webhook` — webhook de confirmação de pagamento

Chamado pela entidade externa que processa o pagamento.

Requisição:
```json
{
  "codigoPagamento": "9c858901-8a57-4791-81fe-4c455b099bc9",
  "status": "APROVADO"
}
```
(`status` também pode ser `"CANCELADO"`.)

Resposta `200 OK` em qualquer chamada válida — **idempotente**: se a venda referenciada já estiver
em estado final (`APROVADO` ou `CANCELADO`), uma nova chamada com o mesmo `codigoPagamento` retorna
`200 OK` sem alterar nada (provedores de pagamento reenviam notificações).

Efeito: `APROVADO` → veículo `VENDIDO`; `CANCELADO` → veículo volta a `DISPONIVEL`.

Falha com `404` se `codigoPagamento` não existir.
