package com.soat.vendaveiculos.venda;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record VendaRequest(
        @NotNull UUID veiculoId,
        @NotNull String cpfComprador,
        @NotNull LocalDate dataVenda
) {
}
