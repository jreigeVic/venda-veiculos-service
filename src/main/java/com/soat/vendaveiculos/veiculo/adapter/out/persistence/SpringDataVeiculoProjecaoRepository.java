package com.soat.vendaveiculos.veiculo.adapter.out.persistence;

import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataVeiculoProjecaoRepository extends JpaRepository<VeiculoProjecaoJpaEntity, UUID> {

    List<VeiculoProjecaoJpaEntity> findByStatusOrderByPrecoAsc(StatusVeiculo status);
}
