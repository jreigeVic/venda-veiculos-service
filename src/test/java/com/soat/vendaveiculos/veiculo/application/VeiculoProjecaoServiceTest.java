package com.soat.vendaveiculos.veiculo.application;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import com.soat.vendaveiculos.veiculo.application.port.in.DadosSincronizacaoVeiculo;
import com.soat.vendaveiculos.veiculo.application.port.out.VeiculoProjecaoRepositoryPort;
import com.soat.vendaveiculos.veiculo.domain.EstadoConservacao;
import com.soat.vendaveiculos.veiculo.domain.StatusVeiculo;
import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoProjecaoServiceTest {

    @Mock
    private VeiculoProjecaoRepositoryPort repository;

    @Mock
    private AuditoriaPort auditoriaService;

    private VeiculoProjecaoService service;

    @BeforeEach
    void setUp() {
        service = new VeiculoProjecaoService(repository, auditoriaService);
    }

    @Test
    void deveCriarProjecaoComoDisponivel() {
        UUID id = UUID.randomUUID();
        DadosSincronizacaoVeiculo dados = new DadosSincronizacaoVeiculo(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO);
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VeiculoProjecao criado = service.criar(dados);

        assertThat(criado.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
        assertThat(criado.getId()).isEqualTo(id);
    }

    @Test
    void deveReprocessarCriacaoSemFalharQuandoIdJaExiste() {
        UUID id = UUID.randomUUID();
        VeiculoProjecao existente = new VeiculoProjecao(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.RESERVADO, 2L);
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DadosSincronizacaoVeiculo dados = new DadosSincronizacaoVeiculo(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO);
        VeiculoProjecao resultado = service.criar(dados);

        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }

    @Test
    void deveAtualizarDadosCadastraisSemAlterarStatus() {
        UUID id = UUID.randomUUID();
        VeiculoProjecao existente = new VeiculoProjecao(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.RESERVADO, null);
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DadosSincronizacaoVeiculo dados = new DadosSincronizacaoVeiculo(id, "Fiat", "Argo", 2022, "Branco",
                BigDecimal.valueOf(76900), EstadoConservacao.SEMINOVO);
        VeiculoProjecao atualizado = service.atualizar(id, dados);

        assertThat(atualizado.getCor()).isEqualTo("Branco");
        assertThat(atualizado.getPreco()).isEqualByComparingTo(BigDecimal.valueOf(76900));
        assertThat(atualizado.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }

    @Test
    void deveFalharAoAtualizarVeiculoInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        DadosSincronizacaoVeiculo dados = new DadosSincronizacaoVeiculo(id, "Fiat", "Argo", 2022, "Branco",
                BigDecimal.valueOf(76900), EstadoConservacao.SEMINOVO);

        assertThatThrownBy(() -> service.atualizar(id, dados))
                .isInstanceOf(VeiculoNaoEncontradoException.class);
    }

    @Test
    void deveListarVeiculosDisponiveisOrdenadosPorPreco() {
        when(repository.findByStatusOrderByPrecoAsc(StatusVeiculo.DISPONIVEL)).thenReturn(List.of());
        List<VeiculoProjecao> resultado = service.listarAVenda();
        assertThat(resultado).isEmpty();
    }

    @Test
    void deveListarVeiculosVendidosOrdenadosPorPreco() {
        when(repository.findByStatusOrderByPrecoAsc(StatusVeiculo.VENDIDO)).thenReturn(List.of());
        List<VeiculoProjecao> resultado = service.listarVendidos();
        assertThat(resultado).isEmpty();
    }

    @Test
    void deveListarVeiculosPorStatusReservado() {
        UUID id = UUID.randomUUID();
        VeiculoProjecao reservado = new VeiculoProjecao(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.RESERVADO, null);
        when(repository.findByStatusOrderByPrecoAsc(StatusVeiculo.RESERVADO)).thenReturn(List.of(reservado));

        List<VeiculoProjecao> resultado = service.listarPorStatus(StatusVeiculo.RESERVADO);

        assertThat(resultado).containsExactly(reservado);
    }
}
