package com.soat.vendaveiculos.veiculo.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class VeiculoProjecao {

    private UUID id;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private BigDecimal preco;
    private EstadoConservacao estadoConservacao;
    private StatusVeiculo status;
    private Long versao;

    public VeiculoProjecao(UUID id, String marca, String modelo, Integer ano, String cor, BigDecimal preco,
                            EstadoConservacao estadoConservacao, StatusVeiculo status, Long versao) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.preco = preco;
        this.estadoConservacao = estadoConservacao;
        this.status = status;
        this.versao = versao;
    }

    public static VeiculoProjecao novaDisponivel(UUID id, String marca, String modelo, Integer ano, String cor,
                                                  BigDecimal preco, EstadoConservacao estadoConservacao) {
        return new VeiculoProjecao(id, marca, modelo, ano, cor, preco, estadoConservacao, StatusVeiculo.DISPONIVEL, null);
    }

    public void atualizarDadosCadastrais(String marca, String modelo, Integer ano, String cor, BigDecimal preco,
                                          EstadoConservacao estadoConservacao) {
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.preco = preco;
        this.estadoConservacao = estadoConservacao;
    }

    public boolean estaDisponivel() {
        return status == StatusVeiculo.DISPONIVEL;
    }

    public void reservar() {
        if (!estaDisponivel()) {
            throw new VeiculoIndisponivelException(id);
        }
        this.status = StatusVeiculo.RESERVADO;
    }

    public void marcarVendido() {
        this.status = StatusVeiculo.VENDIDO;
    }

    public void liberar() {
        this.status = StatusVeiculo.DISPONIVEL;
    }

    public UUID getId() {
        return id;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public String getCor() {
        return cor;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public EstadoConservacao getEstadoConservacao() {
        return estadoConservacao;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public Long getVersao() {
        return versao;
    }
}
