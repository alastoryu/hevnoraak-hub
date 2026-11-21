package br.com.hevnoraak.hub.consulta;

import java.time.Instant;

/**
 * Contrato único de resposta de todas as consultas do Hub.
 *
 * @param status       resultado da consulta
 * @param dados        dados normalizados (DTO específico de cada consulta); nulo quando não encontrado
 * @param fonte        de onde veio a informação (ex.: "BrasilAPI", "ViaCEP")
 * @param consultadoEm momento da consulta na fonte (respostas em cache mantêm o horário original)
 * @param evidencia    resposta original da fonte, guardada para auditoria do caso
 */
public record RespostaConsulta<T>(StatusConsulta status, T dados, String fonte, Instant consultadoEm, String evidencia) {

    public static <T> RespostaConsulta<T> encontrado(T dados, String fonte, String evidencia) {
        return new RespostaConsulta<>(StatusConsulta.ENCONTRADO, dados, fonte, Instant.now(), evidencia);
    }

    public static <T> RespostaConsulta<T> naoEncontrado(String fonte) {
        return new RespostaConsulta<>(StatusConsulta.NAO_ENCONTRADO, null, fonte, Instant.now(), null);
    }
}
