package com.soat.vendaveiculos.veiculo.application.port.in;

import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

import java.util.UUID;

public interface AtualizarProjecaoVeiculoUseCase {

    VeiculoProjecao atualizar(UUID id, DadosSincronizacaoVeiculo dados);
}
