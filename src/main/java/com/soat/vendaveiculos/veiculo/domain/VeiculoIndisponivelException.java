package com.soat.vendaveiculos.veiculo.domain;

import java.util.UUID;

public class VeiculoIndisponivelException extends RuntimeException {

    private final UUID veiculoId;

    public VeiculoIndisponivelException(UUID veiculoId) {
        super("Veículo não está disponível para venda: " + veiculoId);
        this.veiculoId = veiculoId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }
}
