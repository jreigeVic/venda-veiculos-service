package com.soat.vendaveiculos.venda.adapter.out.persistence;

import com.soat.vendaveiculos.venda.domain.Venda;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendaPersistenceAdapterTest {

    @Mock
    private SpringDataVendaRepository jpaRepository;

    @Test
    void deveSalvarEMapearDeVoltaParaDomain() {
        VendaPersistenceAdapter adapter = new VendaPersistenceAdapter(jpaRepository);
        Venda venda = Venda.criar(UUID.randomUUID(), "111.444.777-35", LocalDate.now());
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venda salva = adapter.save(venda);

        assertThat(salva.getVeiculoId()).isEqualTo(venda.getVeiculoId());
    }

    @Test
    void deveBuscarPorCodigoPagamento() {
        VendaPersistenceAdapter adapter = new VendaPersistenceAdapter(jpaRepository);
        Venda venda = Venda.criar(UUID.randomUUID(), "111.444.777-35", LocalDate.now());
        VendaJpaEntity entity = VendaMapper.toJpaEntity(venda);
        when(jpaRepository.findByCodigoPagamento(venda.getCodigoPagamento())).thenReturn(Optional.of(entity));

        Optional<Venda> encontrada = adapter.findByCodigoPagamento(venda.getCodigoPagamento());

        assertThat(encontrada).isPresent();
    }
}
