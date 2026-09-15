package com.soat.vendaveiculos.venda.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record DadosVenda(
        UUID veiculoId,
        String cpfComprador,
        LocalDate dataVenda
) {
}
