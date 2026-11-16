package com.catalogo.filmes.service.exception;

/** O registro pedido não existe (ou não é visível para quem pediu). Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
