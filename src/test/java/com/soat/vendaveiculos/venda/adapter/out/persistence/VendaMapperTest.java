package com.soat.vendaveiculos.venda.adapter.out.persistence;

import com.soat.vendaveiculos.venda.domain.Venda;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VendaMapperTest {

    @Test
    void deveConverterDomainParaJpaEntityEDeVolta() {
        Venda venda = Venda.criar(UUID.randomUUID(), "111.444.777-35", LocalDate.now());

        VendaJpaEntity entity = VendaMapper.toJpaEntity(venda);
        Venda reconvertida = VendaMapper.toDomain(entity);

        assertThat(reconvertida.getVeiculoId()).isEqualTo(venda.getVeiculoId());
        assertThat(reconvertida.getStatusPagamento()).isEqualTo(venda.getStatusPagamento());
        assertThat(reconvertida.getCodigoPagamento()).isEqualTo(venda.getCodigoPagamento());
    }
}
