package com.soat.vendaveiculos.venda.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WebhookRequest(
        @NotNull UUID codigoPagamento,
        @NotNull StatusPagamentoWebhook status
) {

    public enum StatusPagamentoWebhook {
        APROVADO,
        CANCELADO
    }
}
