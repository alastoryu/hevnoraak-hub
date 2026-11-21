package br.com.hevnoraak.hub.consulta;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ErrosHandler {

    @ExceptionHandler(DocumentoInvalidoException.class)
    ProblemDetail documentoInvalido(DocumentoInvalidoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(FonteIndisponivelException.class)
    ProblemDetail fonteIndisponivel(FonteIndisponivelException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        problema.setProperty("tentarNovamente", true);
        return problema;
    }
}
