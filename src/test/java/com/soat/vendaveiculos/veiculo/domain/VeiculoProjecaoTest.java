package com.soat.vendaveiculos.veiculo.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeiculoProjecaoTest {

    private VeiculoProjecao disponivel() {
        return VeiculoProjecao.novaDisponivel(UUID.randomUUID(), "Fiat", "Argo", 2022, "Prata",
                BigDecimal.valueOf(78900), EstadoConservacao.SEMINOVO);
    }

    @Test
    void novaDisponivelComecaComoDisponivel() {
        VeiculoProjecao veiculo = disponivel();

        assertThat(veiculo.estaDisponivel()).isTrue();
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void reservarMudaStatusParaReservadoQuandoDisponivel() {
        VeiculoProjecao veiculo = disponivel();

        veiculo.reservar();

        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }

    @Test
    void reservarLancaExcecaoQuandoNaoDisponivel() {
        VeiculoProjecao veiculo = disponivel();
        veiculo.reservar();

        assertThatThrownBy(veiculo::reservar).isInstanceOf(VeiculoIndisponivelException.class);
    }

    @Test
    void marcarVendidoMudaStatusParaVendido() {
        VeiculoProjecao veiculo = disponivel();
        veiculo.reservar();

        veiculo.marcarVendido();

        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.VENDIDO);
    }

    @Test
    void liberarVoltaStatusParaDisponivel() {
        VeiculoProjecao veiculo = disponivel();
        veiculo.reservar();

        veiculo.liberar();

        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void atualizarDadosCadastraisNaoAlteraStatus() {
        VeiculoProjecao veiculo = disponivel();
        veiculo.reservar();

        veiculo.atualizarDadosCadastrais("Fiat", "Argo", 2022, "Branco", BigDecimal.valueOf(76900), EstadoConservacao.SEMINOVO);

        assertThat(veiculo.getCor()).isEqualTo("Branco");
        assertThat(veiculo.getPreco()).isEqualByComparingTo(BigDecimal.valueOf(76900));
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }
}
