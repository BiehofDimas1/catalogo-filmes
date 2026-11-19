package com.catalogo.filmes.service.exception;

public class CredenciaisInvalidasException extends ErroDaAplicacao {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int status() {
        return 401;
    }

    @Override
    public String titulo() {
        return "Não autenticado";
    }
}
