package com.catalogo.filmes.service.exception;

public class RecursoNaoEncontradoException extends ErroDaAplicacao {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int status() {
        return 404;
    }

    @Override
    public String titulo() {
        return "Não encontrado";
    }
}
