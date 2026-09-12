package com.soat.vendaveiculos.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, UUID> {
}
