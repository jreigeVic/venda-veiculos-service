package com.soat.vendaveiculos.venda;

public final class CpfValidador {

    private CpfValidador() {
    }

    public static boolean isValido(String cpf) {
        if (cpf == null) {
            return false;
        }
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitos.charAt(9) - '0' == calcularDigito(digitos, 9)
                && digitos.charAt(10) - '0' == calcularDigito(digitos, 10);
    }

    private static int calcularDigito(String digitos, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * peso;
            peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
