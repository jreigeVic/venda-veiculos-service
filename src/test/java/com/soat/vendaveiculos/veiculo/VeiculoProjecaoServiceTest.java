package com.soat.vendaveiculos.veiculo;

import com.soat.vendaveiculos.auditoria.AuditoriaService;
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
    private VeiculoProjecaoRepository repository;

    @Mock
    private AuditoriaService auditoriaService;

    private VeiculoProjecaoService service;

    @BeforeEach
    void setUp() {
        service = new VeiculoProjecaoService(repository, auditoriaService);
    }

    @Test
    void deveCriarProjecaoComoDisponivel() {
        UUID id = UUID.randomUUID();
        VeiculoSyncRequest request = new VeiculoSyncRequest(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VeiculoProjecao criado = service.criar(request);

        assertThat(criado.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
        assertThat(criado.getId()).isEqualTo(id);
    }

    @Test
    void deveAtualizarDadosCadastraisSemAlterarStatus() {
        UUID id = UUID.randomUUID();
        VeiculoProjecao existente = new VeiculoProjecao(id, "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO, StatusVeiculo.RESERVADO, null);
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VeiculoSyncRequest request = new VeiculoSyncRequest(id, "Fiat", "Argo", 2022, "Branco",
                BigDecimal.valueOf(76900), EstadoConservacao.SEMINOVO);
        VeiculoProjecao atualizado = service.atualizar(id, request);

        assertThat(atualizado.getCor()).isEqualTo("Branco");
        assertThat(atualizado.getPreco()).isEqualByComparingTo(BigDecimal.valueOf(76900));
        assertThat(atualizado.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }

    @Test
    void deveFalharAoAtualizarVeiculoInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        VeiculoSyncRequest request = new VeiculoSyncRequest(id, "Fiat", "Argo", 2022, "Branco",
                BigDecimal.valueOf(76900), EstadoConservacao.SEMINOVO);

        assertThatThrownBy(() -> service.atualizar(id, request))
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
}
