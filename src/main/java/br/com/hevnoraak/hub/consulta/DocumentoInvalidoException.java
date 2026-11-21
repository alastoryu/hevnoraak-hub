package br.com.hevnoraak.hub.consulta;

public class DocumentoInvalidoException extends RuntimeException {

    public DocumentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
