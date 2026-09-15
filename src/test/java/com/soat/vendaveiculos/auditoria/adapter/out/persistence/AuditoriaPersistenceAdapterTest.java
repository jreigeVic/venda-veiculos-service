package com.soat.vendaveiculos.auditoria.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditoriaPersistenceAdapterTest {

    @Mock
    private SpringDataLogAuditoriaRepository repository;

    @Test
    void deveRegistrarSucesso() {
        AuditoriaPersistenceAdapter adapter = new AuditoriaPersistenceAdapter(repository);
        UUID id = UUID.randomUUID();

        adapter.registrarSucesso("OPERACAO_X", id, "detalhe");

        ArgumentCaptor<LogAuditoriaJpaEntity> captor = ArgumentCaptor.forClass(LogAuditoriaJpaEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getResultado()).isEqualTo(Resultado.SUCESSO);
        assertThat(captor.getValue().getEntidadeId()).isEqualTo(id);
    }

    @Test
    void deveRegistrarErro() {
        AuditoriaPersistenceAdapter adapter = new AuditoriaPersistenceAdapter(repository);
        UUID id = UUID.randomUUID();

        adapter.registrarErro("OPERACAO_Y", id, "falhou");

        ArgumentCaptor<LogAuditoriaJpaEntity> captor = ArgumentCaptor.forClass(LogAuditoriaJpaEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getResultado()).isEqualTo(Resultado.ERRO);
    }
}
