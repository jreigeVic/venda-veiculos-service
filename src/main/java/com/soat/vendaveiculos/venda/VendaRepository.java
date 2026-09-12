package com.soat.vendaveiculos.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {

    Optional<Venda> findByCodigoPagamento(UUID codigoPagamento);
}
