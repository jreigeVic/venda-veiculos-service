package com.soat.vendaveiculos.venda.adapter.out.persistence;

import com.soat.vendaveiculos.venda.domain.Venda;

public final class VendaMapper {

    private VendaMapper() {
    }

    public static Venda toDomain(VendaJpaEntity entity) {
        return new Venda(
                entity.getId(),
                entity.getVeiculoId(),
                entity.getCpfComprador(),
                entity.getDataVenda(),
                entity.getCodigoPagamento(),
                entity.getStatusPagamento(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    public static VendaJpaEntity toJpaEntity(Venda venda) {
        return new VendaJpaEntity(
                venda.getId(),
                venda.getVeiculoId(),
                venda.getCpfComprador(),
                venda.getDataVenda(),
                venda.getCodigoPagamento(),
                venda.getStatusPagamento(),
                venda.getCriadoEm(),
                venda.getAtualizadoEm()
        );
    }
}
