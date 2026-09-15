package com.soat.vendaveiculos.veiculo.adapter.out.persistence;

import com.soat.vendaveiculos.veiculo.application.port.out.VeiculoProjecaoRepositoryPort;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VeiculoProjecaoPersistenceAdapter implements VeiculoProjecaoRepositoryPort {

    private final SpringDataVeiculoProjecaoRepository jpaRepository;

    public VeiculoProjecaoPersistenceAdapter(SpringDataVeiculoProjecaoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VeiculoProjecao save(VeiculoProjecao veiculo) {
        VeiculoProjecaoJpaEntity salvo = jpaRepository.save(VeiculoProjecaoMapper.toJpaEntity(veiculo));
        return VeiculoProjecaoMapper.toDomain(salvo);
    }

    @Override
    public Optional<VeiculoProjecao> findById(UUID id) {
        return jpaRepository.findById(id).map(VeiculoProjecaoMapper::toDomain);
    }

    @Override
    public List<VeiculoProjecao> findByStatusOrderByPrecoAsc(StatusVeiculo status) {
        return jpaRepository.findByStatusOrderByPrecoAsc(status).stream()
                .map(VeiculoProjecaoMapper::toDomain)
                .toList();
    }
}
