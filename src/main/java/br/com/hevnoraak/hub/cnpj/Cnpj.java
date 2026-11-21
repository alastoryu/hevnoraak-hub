package br.com.hevnoraak.hub.cnpj;

import br.com.hevnoraak.hub.consulta.DocumentoInvalidoException;

/**
 * CNPJ validado, já no formato alfanumérico adotado pela Receita a partir de julho de 2026
 * (12 posições alfanuméricas + 2 dígitos verificadores numéricos). CNPJs só numéricos continuam válidos.
 */
public record Cnpj(String valor) {

    private static final int[] PESOS_DV1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_DV2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (!valido(valor)) {
            throw new DocumentoInvalidoException("CNPJ inválido: " + valor);
        }
    }

    /** Aceita com ou sem máscara: "12.ABC.345/01DE-35" ou "12ABC34501DE35". */
    public static Cnpj de(String entrada) {
        String limpo = entrada == null ? "" : entrada.replaceAll("[./-]", "").toUpperCase();
        return new Cnpj(limpo);
    }

    static boolean valido(String c) {
        if (c == null || !c.matches("[0-9A-Z]{12}[0-9]{2}") || c.chars().distinct().count() == 1) {
            return false;
        }
        int dv1 = digito(c, PESOS_DV1);
        int dv2 = digito(c, PESOS_DV2);
        return c.charAt(12) - '0' == dv1 && c.charAt(13) - '0' == dv2;
    }

    /** Cada caractere vale seu código ASCII menos 48 (0-9 → 0-9, A-Z → 17-42). */
    private static int digito(String c, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (c.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
