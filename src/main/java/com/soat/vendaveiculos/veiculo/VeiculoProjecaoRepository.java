package com.soat.vendaveiculos.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VeiculoProjecaoRepository extends JpaRepository<VeiculoProjecao, UUID> {

    List<VeiculoProjecao> findByStatusOrderByPrecoAsc(StatusVeiculo status);
}
