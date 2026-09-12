"""Mock da processadora de pagamento.

Simula, no formato de um handler AWS Lambda (assinatura `lambda_handler(event, context)`),
a notificação assíncrona que uma processadora de pagamento real enviaria para o webhook do
serviço de venda de veículos. Executado localmente (sem credenciais AWS — ver
docs/decisoes-pendentes.md, item 3 e item 7); pode ser publicado como Lambda de verdade
depois, sem alterar o handler, só o gatilho.

Uso local (CLI):
    python handler.py <codigoPagamento> <APROVADO|CANCELADO> [baseUrl]

Uso como handler Lambda (event de exemplo):
    {
      "codigoPagamento": "9c858901-8a57-4791-81fe-4c455b099bc9",
      "status": "APROVADO",
      "baseUrl": "http://localhost:8081"
    }
"""

import json
import sys
import urllib.error
import urllib.request

DEFAULT_BASE_URL = "http://localhost:8081"


def lambda_handler(event, context=None):
    codigo_pagamento = event["codigoPagamento"]
    status = event["status"]
    base_url = event.get("baseUrl", DEFAULT_BASE_URL)

    payload = json.dumps({"codigoPagamento": codigo_pagamento, "status": status}).encode("utf-8")
    request = urllib.request.Request(
        url=f"{base_url}/pagamentos/webhook",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    try:
        with urllib.request.urlopen(request) as response:
            return {"statusCode": response.status, "body": "webhook notificado com sucesso"}
    except urllib.error.HTTPError as e:
        return {"statusCode": e.code, "body": e.read().decode("utf-8")}


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Uso: python handler.py <codigoPagamento> <APROVADO|CANCELADO> [baseUrl]")
        sys.exit(1)

    evento = {"codigoPagamento": sys.argv[1], "status": sys.argv[2]}
    if len(sys.argv) > 3:
        evento["baseUrl"] = sys.argv[3]

    resultado = lambda_handler(evento)
    print(resultado)
