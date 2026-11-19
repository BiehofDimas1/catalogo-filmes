package com.catalogo.filmes.service.exception;

public abstract class ErroDaAplicacao extends RuntimeException {

    protected ErroDaAplicacao(String mensagem) {
        super(mensagem);
    }

    public abstract int status();

    public abstract String titulo();
}
