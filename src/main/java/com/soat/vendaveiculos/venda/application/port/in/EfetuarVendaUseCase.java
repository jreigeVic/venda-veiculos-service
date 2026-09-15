package com.soat.vendaveiculos.venda.application.port.in;

import com.soat.vendaveiculos.venda.domain.Venda;

public interface EfetuarVendaUseCase {

    Venda efetuar(DadosVenda dados);
}
