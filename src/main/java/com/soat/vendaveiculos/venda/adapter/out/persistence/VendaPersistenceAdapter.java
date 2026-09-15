package com.soat.vendaveiculos.venda.adapter.out.persistence;

import com.soat.vendaveiculos.venda.application.port.out.VendaRepositoryPort;
import com.soat.vendaveiculos.venda.domain.Venda;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VendaPersistenceAdapter implements VendaRepositoryPort {

    private final SpringDataVendaRepository jpaRepository;

    public VendaPersistenceAdapter(SpringDataVendaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Venda save(Venda venda) {
        VendaJpaEntity salvo = jpaRepository.save(VendaMapper.toJpaEntity(venda));
        return VendaMapper.toDomain(salvo);
    }

    @Override
    public Optional<Venda> findByCodigoPagamento(UUID codigoPagamento) {
        return jpaRepository.findByCodigoPagamento(codigoPagamento).map(VendaMapper::toDomain);
    }
}
