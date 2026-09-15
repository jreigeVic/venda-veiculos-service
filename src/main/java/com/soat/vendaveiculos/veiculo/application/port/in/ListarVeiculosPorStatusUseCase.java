package com.soat.vendaveiculos.veiculo.application.port.in;

import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

import java.util.List;

public interface ListarVeiculosPorStatusUseCase {

    List<VeiculoProjecao> listarPorStatus(StatusVeiculo status);
}
