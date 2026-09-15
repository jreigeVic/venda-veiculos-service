package com.soat.vendaveiculos.venda.adapter.in.web;

import com.soat.vendaveiculos.venda.domain.StatusPagamento;
import com.soat.vendaveiculos.venda.domain.Venda;

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
