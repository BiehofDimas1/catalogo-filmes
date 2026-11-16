package com.catalogo.filmes.service.exception;

/** E-mail ou senha incorretos no login. Vira HTTP 401. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
