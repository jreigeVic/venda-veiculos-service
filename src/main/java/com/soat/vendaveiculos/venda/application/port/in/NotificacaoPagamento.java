package com.soat.vendaveiculos.venda.application.port.in;

import java.util.UUID;

public record NotificacaoPagamento(
        UUID codigoPagamento,
        Status status
) {

    public enum Status {
        APROVADO,
        CANCELADO
    }
}
