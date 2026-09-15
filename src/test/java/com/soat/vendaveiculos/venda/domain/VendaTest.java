package com.soat.vendaveiculos.venda.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VendaTest {

    @Test
    void criarComecaComoPendente() {
        Venda venda = Venda.criar(UUID.randomUUID(), "111.444.777-35", LocalDate.now());

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(venda.getCodigoPagamento()).isNotNull();
        assertThat(venda.estaEmEstadoFinal()).isFalse();
    }

    @Test
    void aprovarMudaStatusParaAprovadoEEstadoFinal() {
        Venda venda = Venda.criar(UUID.randomUUID(), "111.444.777-35", LocalDate.now());

        venda.aprovar();

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(venda.estaEmEstadoFinal()).isTrue();
    }

    @Test
    void cancelarMudaStatusParaCanceladoEEstadoFinal() {
        Venda venda = Venda.criar(UUID.randomUUID(), "111.444.777-35", LocalDate.now());

        venda.cancelar();

        assertThat(venda.getStatusPagamento()).isEqualTo(StatusPagamento.CANCELADO);
        assertThat(venda.estaEmEstadoFinal()).isTrue();
    }
}
