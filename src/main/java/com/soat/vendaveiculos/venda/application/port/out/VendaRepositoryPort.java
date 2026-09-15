package com.soat.vendaveiculos.venda.application.port.out;

import com.soat.vendaveiculos.venda.domain.Venda;

import java.util.Optional;
import java.util.UUID;

public interface VendaRepositoryPort {

    Venda save(Venda venda);

    Optional<Venda> findByCodigoPagamento(UUID codigoPagamento);
}
