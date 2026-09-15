package com.soat.vendaveiculos.veiculo.adapter.out.persistence;

import com.soat.vendaveiculos.veiculo.domain.VeiculoProjecao;

public final class VeiculoProjecaoMapper {

    private VeiculoProjecaoMapper() {
    }

    public static VeiculoProjecao toDomain(VeiculoProjecaoJpaEntity entity) {
        return new VeiculoProjecao(
                entity.getId(),
                entity.getMarca(),
                entity.getModelo(),
                entity.getAno(),
                entity.getCor(),
                entity.getPreco(),
                entity.getEstadoConservacao(),
                entity.getStatus(),
                entity.getVersao()
        );
    }

    public static VeiculoProjecaoJpaEntity toJpaEntity(VeiculoProjecao veiculo) {
        return new VeiculoProjecaoJpaEntity(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPreco(),
                veiculo.getEstadoConservacao(),
                veiculo.getStatus(),
                veiculo.getVersao()
        );
    }
}
