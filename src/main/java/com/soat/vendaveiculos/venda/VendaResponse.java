package com.soat.vendaveiculos.venda;

import java.time.LocalDate;
import java.util.UUID;

public record VendaResponse(
        UUID id,
        UUID veiculoId,
        String cpfComprador,
        LocalDate dataVenda,
        UUID codigoPagamento,
        StatusPagamento statusPagamento
) {

    public static VendaResponse de(Venda venda) {
        return new VendaResponse(
                venda.getId(),
                venda.getVeiculoId(),
                venda.getCpfComprador(),
                venda.getDataVenda(),
                venda.getCodigoPagamento(),
                venda.getStatusPagamento()
        );
    }
}
