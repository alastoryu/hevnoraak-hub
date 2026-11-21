package br.com.hevnoraak.hub.consulta;

/** A fonte externa falhou ou não respondeu a tempo. O orquestrador deve tentar de novo mais tarde. */
public class FonteIndisponivelException extends RuntimeException {

    public FonteIndisponivelException(String fonte, Throwable causa) {
        super("Fonte indisponível: " + fonte, causa);
    }
}
