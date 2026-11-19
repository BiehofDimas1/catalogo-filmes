package com.catalogo.filmes.service.exception;

public class VersaoDesatualizadaException extends ErroDaAplicacao {

    public VersaoDesatualizadaException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int status() {
        return 409;
    }

    @Override
    public String titulo() {
        return "Conflito de atualização";
    }
}
