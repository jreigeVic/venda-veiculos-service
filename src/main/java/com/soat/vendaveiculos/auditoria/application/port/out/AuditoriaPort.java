package com.soat.vendaveiculos.auditoria.application.port.out;

import java.util.UUID;

public interface AuditoriaPort {

    void registrarSucesso(String operacao, UUID entidadeId, String detalhe);

    void registrarErro(String operacao, UUID entidadeId, String detalhe);
}
