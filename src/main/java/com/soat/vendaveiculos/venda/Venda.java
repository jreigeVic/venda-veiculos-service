package com.soat.vendaveiculos.venda;

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
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "venda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID veiculoId;

    private String cpfComprador;

    private LocalDate dataVenda;

    private UUID codigoPagamento;

    @Enumerated(EnumType.STRING)
    private StatusPagamento statusPagamento;

    private Instant criadoEm;

    private Instant atualizadoEm;

    public boolean estaEmEstadoFinal() {
        return statusPagamento == StatusPagamento.APROVADO || statusPagamento == StatusPagamento.CANCELADO;
    }
}
