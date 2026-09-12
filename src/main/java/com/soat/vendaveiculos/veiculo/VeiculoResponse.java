package com.soat.vendaveiculos.veiculo;

import java.math.BigDecimal;
import java.util.UUID;

public record VeiculoResponse(
        UUID id,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        BigDecimal preco,
        EstadoConservacao estadoConservacao,
        StatusVeiculo status
) {

    public static VeiculoResponse de(VeiculoProjecao veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPreco(),
                veiculo.getEstadoConservacao(),
                veiculo.getStatus()
        );
    }
}
