package com.catalogo.filmes.service.exception;

/** A edição partiu de uma versão antiga do registro, que mudou nesse meio-tempo. Vira HTTP 409. */
public class VersaoDesatualizadaException extends RuntimeException {

    public VersaoDesatualizadaException(String mensagem) {
        super(mensagem);
    }
}
