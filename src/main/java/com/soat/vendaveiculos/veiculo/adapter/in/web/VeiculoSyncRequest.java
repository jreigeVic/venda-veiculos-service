package com.soat.vendaveiculos.veiculo.adapter.in.web;

import com.soat.vendaveiculos.veiculo.domain.EstadoConservacao;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record VeiculoSyncRequest(
        @NotNull UUID id,
        @NotNull String marca,
        @NotNull String modelo,
        @NotNull Integer ano,
        @NotNull String cor,
        @NotNull BigDecimal preco,
        @NotNull EstadoConservacao estadoConservacao
) {
}
