package com.soat.vendaveiculos.auditoria;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_auditoria")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String servico;

    private String operacao;

    private UUID entidadeId;

    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    private String detalhe;

    private Instant criadoEm;

    public static LogAuditoria sucesso(String operacao, UUID entidadeId, String detalhe) {
        return new LogAuditoria(null, "venda-veiculos-service", operacao, entidadeId, Resultado.SUCESSO, detalhe, Instant.now());
    }

    public static LogAuditoria erro(String operacao, UUID entidadeId, String detalhe) {
        return new LogAuditoria(null, "venda-veiculos-service", operacao, entidadeId, Resultado.ERRO, detalhe, Instant.now());
    }
}
