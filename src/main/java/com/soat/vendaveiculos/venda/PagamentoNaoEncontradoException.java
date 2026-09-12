package com.soat.vendaveiculos.venda;

import java.util.UUID;

public class PagamentoNaoEncontradoException extends RuntimeException {

    private final UUID codigoPagamento;

    public PagamentoNaoEncontradoException(UUID codigoPagamento) {
        super("Código de pagamento não encontrado: " + codigoPagamento);
        this.codigoPagamento = codigoPagamento;
    }

    public UUID getCodigoPagamento() {
        return codigoPagamento;
    }
}
