package com.soat.vendaveiculos.veiculo.application.port.in;

import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

import java.util.List;

public interface ListarVeiculosVendidosUseCase {

    List<VeiculoProjecao> listarVendidos();
}
