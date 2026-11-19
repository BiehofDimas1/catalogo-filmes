package com.catalogo.filmes.service.exception;

public class AcessoNegadoException extends ErroDaAplicacao {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int status() {
        return 403;
    }

    @Override
    public String titulo() {
        return "Acesso negado";
    }
}
