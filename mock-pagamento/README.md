# Mock da processadora de pagamento

Simula a notificação assíncrona de uma processadora de pagamento real, no formato de um handler
AWS Lambda (`lambda_handler(event, context)`), sem depender de credenciais AWS — ver
[`../docs/decisoes-pendentes.md`](../docs/decisoes-pendentes.md), itens 3 e 7. Quando houver
credenciais reais, o mesmo `handler.py` pode ser publicado como Lambda de verdade sem alterações
no código, só no gatilho de invocação.

## Uso local

```bash
python handler.py <codigoPagamento> APROVADO
python handler.py <codigoPagamento> CANCELADO http://localhost:8081
```

`codigoPagamento` é o valor retornado por `POST /vendas` (ver
[`../docs/contratos-api.md`](../docs/contratos-api.md)). Usado no vídeo de demonstração para
automatizar a confirmação de pagamento sem precisar de uma chamada manual via Postman.
