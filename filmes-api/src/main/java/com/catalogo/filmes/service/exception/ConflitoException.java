package com.catalogo.filmes.service.exception;

public class ConflitoException extends ErroDaAplicacao {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int status() {
        return 409;
    }

    @Override
    public String titulo() {
        return "Registro duplicado";
    }
}
