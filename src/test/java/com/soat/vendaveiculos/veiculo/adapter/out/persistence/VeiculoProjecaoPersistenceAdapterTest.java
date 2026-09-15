package com.soat.vendaveiculos.veiculo.adapter.out.persistence;

import com.soat.vendaveiculos.veiculo.domain.EstadoConservacao;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoProjecaoPersistenceAdapterTest {

    @Mock
    private SpringDataVeiculoProjecaoRepository jpaRepository;

    @Test
    void deveSalvarEMapearDeVoltaParaDomain() {
        VeiculoProjecaoPersistenceAdapter adapter = new VeiculoProjecaoPersistenceAdapter(jpaRepository);
        VeiculoProjecao veiculo = VeiculoProjecao.novaDisponivel(UUID.randomUUID(), "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VeiculoProjecao salvo = adapter.save(veiculo);

        assertThat(salvo.getId()).isEqualTo(veiculo.getId());
    }

    @Test
    void deveBuscarPorIdQuandoEncontrado() {
        VeiculoProjecaoPersistenceAdapter adapter = new VeiculoProjecaoPersistenceAdapter(jpaRepository);
        UUID id = UUID.randomUUID();
        VeiculoProjecaoJpaEntity entity = new VeiculoProjecaoJpaEntity(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.DISPONIVEL, 0L);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<VeiculoProjecao> encontrado = adapter.findById(id);

        assertThat(encontrado).isPresent();
    }

    @Test
    void deveListarPorStatus() {
        VeiculoProjecaoPersistenceAdapter adapter = new VeiculoProjecaoPersistenceAdapter(jpaRepository);
        when(jpaRepository.findByStatusOrderByPrecoAsc(eq(StatusVeiculo.DISPONIVEL))).thenReturn(List.of());

        assertThat(adapter.findByStatusOrderByPrecoAsc(StatusVeiculo.DISPONIVEL)).isEmpty();
    }
}
