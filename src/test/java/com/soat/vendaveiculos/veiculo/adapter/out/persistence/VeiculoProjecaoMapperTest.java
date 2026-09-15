package com.soat.vendaveiculos.veiculo.adapter.out.persistence;

import com.soat.vendaveiculos.veiculo.domain.EstadoConservacao;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoProjecaoMapperTest {

    @Test
    void deveConverterDomainParaJpaEntityEDeVoltaPreservandoVersao() {
        VeiculoProjecao veiculo = new VeiculoProjecao(UUID.randomUUID(), "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.RESERVADO, 3L);

        VeiculoProjecaoJpaEntity entity = VeiculoProjecaoMapper.toJpaEntity(veiculo);
        VeiculoProjecao reconvertido = VeiculoProjecaoMapper.toDomain(entity);

        assertThat(entity.getVersao()).isEqualTo(3L);
        assertThat(reconvertido.getVersao()).isEqualTo(3L);
        assertThat(reconvertido.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
        assertThat(reconvertido.getId()).isEqualTo(veiculo.getId());
    }
}
