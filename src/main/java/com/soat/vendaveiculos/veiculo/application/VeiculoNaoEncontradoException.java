package com.soat.vendaveiculos.veiculo.application;

import java.util.UUID;

public class VeiculoNaoEncontradoException extends RuntimeException {

    private final UUID veiculoId;

    public VeiculoNaoEncontradoException(UUID id) {
        super("Veículo não encontrado: " + id);
        this.veiculoId = id;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }
}
