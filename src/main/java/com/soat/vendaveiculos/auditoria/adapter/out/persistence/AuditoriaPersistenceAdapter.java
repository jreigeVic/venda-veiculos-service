package com.soat.vendaveiculos.auditoria.adapter.out.persistence;

import com.soat.vendaveiculos.auditoria.application.port.out.AuditoriaPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditoriaPersistenceAdapter implements AuditoriaPort {

    private final SpringDataLogAuditoriaRepository repository;

    public AuditoriaPersistenceAdapter(SpringDataLogAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void registrarSucesso(String operacao, UUID entidadeId, String detalhe) {
        repository.save(LogAuditoriaJpaEntity.sucesso(operacao, entidadeId, detalhe));
    }

    @Override
    public void registrarErro(String operacao, UUID entidadeId, String detalhe) {
        repository.save(LogAuditoriaJpaEntity.erro(operacao, entidadeId, detalhe));
    }
}
