package com.soat.vendaveiculos.veiculo.application.port.in;

import com.soat.vendaveiculos.veiculo.domain.EstadoConservacao;

import java.math.BigDecimal;
import java.util.UUID;

public record DadosSincronizacaoVeiculo(
        UUID id,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        BigDecimal preco,
        EstadoConservacao estadoConservacao
) {
}
