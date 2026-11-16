package com.catalogo.filmes.service.exception;

/** A operação duplicaria um registro existente (título, e-mail, aluguel ativo). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
