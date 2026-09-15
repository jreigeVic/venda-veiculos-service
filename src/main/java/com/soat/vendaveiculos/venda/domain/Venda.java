package com.soat.vendaveiculos.venda.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Venda {

    private UUID id;
    private UUID veiculoId;
    private String cpfComprador;
    private LocalDate dataVenda;
    private UUID codigoPagamento;
    private StatusPagamento statusPagamento;
    private Instant criadoEm;
    private Instant atualizadoEm;

    public Venda(UUID id, UUID veiculoId, String cpfComprador, LocalDate dataVenda, UUID codigoPagamento,
                 StatusPagamento statusPagamento, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.veiculoId = veiculoId;
        this.cpfComprador = cpfComprador;
        this.dataVenda = dataVenda;
        this.codigoPagamento = codigoPagamento;
        this.statusPagamento = statusPagamento;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Venda criar(UUID veiculoId, String cpfComprador, LocalDate dataVenda) {
        Instant agora = Instant.now();
        return new Venda(null, veiculoId, cpfComprador, dataVenda, UUID.randomUUID(), StatusPagamento.PENDENTE, agora, agora);
    }

    public boolean estaEmEstadoFinal() {
        return statusPagamento == StatusPagamento.APROVADO || statusPagamento == StatusPagamento.CANCELADO;
    }

    public void aprovar() {
        this.statusPagamento = StatusPagamento.APROVADO;
        this.atualizadoEm = Instant.now();
    }

    public void cancelar() {
        this.statusPagamento = StatusPagamento.CANCELADO;
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public String getCpfComprador() {
        return cpfComprador;
    }

    public LocalDate getDataVenda() {
        return dataVenda;
    }

    public UUID getCodigoPagamento() {
        return codigoPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
