package com.soat.vendaveiculos.veiculo.application.port.in;

import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

import java.util.List;

public interface ListarVeiculosAVendaUseCase {

    List<VeiculoProjecao> listarAVenda();
}
