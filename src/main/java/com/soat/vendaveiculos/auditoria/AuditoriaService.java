package com.soat.vendaveiculos.auditoria;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository repository;

    public AuditoriaService(LogAuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrarSucesso(String operacao, UUID entidadeId, String detalhe) {
        repository.save(LogAuditoria.sucesso(operacao, entidadeId, detalhe));
    }

    public void registrarErro(String operacao, UUID entidadeId, String detalhe) {
        repository.save(LogAuditoria.erro(operacao, entidadeId, detalhe));
    }
}
