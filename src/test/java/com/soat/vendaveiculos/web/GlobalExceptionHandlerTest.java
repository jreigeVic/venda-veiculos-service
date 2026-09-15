package com.soat.vendaveiculos.web;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private AuditoriaPort auditoriaService;

    @Test
    void deveConverterViolacaoDeIntegridadeEmConflitoSemVazarDetalheInterno() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(auditoriaService);
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; constraint [veiculo_projecao_pkey]");

        ResponseEntity<ErroResponse> resposta = handler.tratar(ex);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Conflito ao persistir os dados");
        assertThat(resposta.getBody().mensagem()).doesNotContain("constraint", "veiculo_projecao_pkey");
        verify(auditoriaService).registrarErro(eq("VIOLACAO_INTEGRIDADE_DADOS"), isNull(), any());
    }
}
