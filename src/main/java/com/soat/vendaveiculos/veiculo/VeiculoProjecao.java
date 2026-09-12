package com.soat.vendaveiculos.veiculo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "veiculo_projecao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoProjecao {

    @Id
    private UUID id;

    private String marca;

    private String modelo;

    private Integer ano;

    private String cor;

    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    private EstadoConservacao estadoConservacao;

    @Enumerated(EnumType.STRING)
    private StatusVeiculo status;

    @Version
    private Long versao;

    public boolean estaDisponivel() {
        return status == StatusVeiculo.DISPONIVEL;
    }
}
