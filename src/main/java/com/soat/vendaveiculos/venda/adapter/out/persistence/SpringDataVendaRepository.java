package com.soat.vendaveiculos.venda.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataVendaRepository extends JpaRepository<VendaJpaEntity, UUID> {

    Optional<VendaJpaEntity> findByCodigoPagamento(UUID codigoPagamento);
}
