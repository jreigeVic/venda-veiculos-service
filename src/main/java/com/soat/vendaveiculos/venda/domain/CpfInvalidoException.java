package com.soat.vendaveiculos.venda.domain;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException(String cpf) {
        super("CPF inválido: " + cpf);
    }
}
