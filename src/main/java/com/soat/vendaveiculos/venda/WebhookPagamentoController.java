package com.soat.vendaveiculos.venda;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
public class WebhookPagamentoController {

    private final VendaService service;

    public WebhookPagamentoController(VendaService service) {
        this.service = service;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receber(@Valid @RequestBody WebhookRequest request) {
        service.processarWebhook(request);
        return ResponseEntity.ok().build();
    }
}
