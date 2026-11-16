package com.catalogo.filmes.service.exception;

/** O usuário está logado, mas o registro pertence a outra pessoa. Vira HTTP 403. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
