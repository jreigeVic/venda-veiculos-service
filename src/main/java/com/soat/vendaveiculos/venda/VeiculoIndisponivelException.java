package com.soat.vendaveiculos.venda;

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
