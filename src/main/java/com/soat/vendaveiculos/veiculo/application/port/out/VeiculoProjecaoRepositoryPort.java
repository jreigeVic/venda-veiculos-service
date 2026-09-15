package com.soat.vendaveiculos.veiculo.application.port.out;

import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoProjecaoRepositoryPort {

    VeiculoProjecao save(VeiculoProjecao veiculo);

    Optional<VeiculoProjecao> findById(UUID id);

    List<VeiculoProjecao> findByStatusOrderByPrecoAsc(StatusVeiculo status);
}
