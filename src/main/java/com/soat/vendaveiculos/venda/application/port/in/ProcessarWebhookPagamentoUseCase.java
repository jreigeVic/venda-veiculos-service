package com.soat.vendaveiculos.venda.application.port.in;

public interface ProcessarWebhookPagamentoUseCase {

    void processar(NotificacaoPagamento notificacao);
}
