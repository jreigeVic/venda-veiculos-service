package com.soat.vendaveiculos.veiculo.application.port.in;

import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

public interface CriarProjecaoVeiculoUseCase {

    VeiculoProjecao criar(DadosSincronizacaoVeiculo dados);
}
