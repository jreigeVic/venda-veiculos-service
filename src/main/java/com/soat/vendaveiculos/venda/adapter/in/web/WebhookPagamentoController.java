package com.soat.vendaveiculos.venda.adapter.in.web;

import com.soat.vendaveiculos.venda.application.port.in.NotificacaoPagamento;
import com.soat.vendaveiculos.venda.application.port.in.ProcessarWebhookPagamentoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
public class WebhookPagamentoController {

    private final ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;

    public WebhookPagamentoController(ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase) {
        this.processarWebhookPagamentoUseCase = processarWebhookPagamentoUseCase;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receber(@Valid @RequestBody WebhookRequest request) {
        NotificacaoPagamento.Status status = request.status() == WebhookRequest.StatusPagamentoWebhook.APROVADO
                ? NotificacaoPagamento.Status.APROVADO
                : NotificacaoPagamento.Status.CANCELADO;
        processarWebhookPagamentoUseCase.processar(new NotificacaoPagamento(request.codigoPagamento(), status));
        return ResponseEntity.ok().build();
    }
}
