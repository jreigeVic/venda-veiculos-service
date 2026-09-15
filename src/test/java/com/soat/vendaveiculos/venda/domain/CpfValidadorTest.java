package com.soat.vendaveiculos.venda.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidadorTest {

    @Test
    void deveAceitarCpfValidoComFormatacao() {
        assertThat(CpfValidador.isValido("111.444.777-35")).isTrue();
    }

    @Test
    void deveAceitarCpfValidoSemFormatacao() {
        assertThat(CpfValidador.isValido("11144477735")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "00000000000", "123.456.789-00", "1234567890", "123456789012"})
    void deveRejeitarCpfInvalido(String cpf) {
        assertThat(CpfValidador.isValido(cpf)).isFalse();
    }

    @Test
    void deveRejeitarCpfNulo() {
        assertThat(CpfValidador.isValido(null)).isFalse();
    }
}
