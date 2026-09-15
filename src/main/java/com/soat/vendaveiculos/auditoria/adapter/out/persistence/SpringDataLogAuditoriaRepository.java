package com.soat.vendaveiculos.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataLogAuditoriaRepository extends JpaRepository<LogAuditoriaJpaEntity, UUID> {
}
