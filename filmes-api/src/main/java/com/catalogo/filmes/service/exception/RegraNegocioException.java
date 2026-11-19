package com.catalogo.filmes.service.exception;

public class RegraNegocioException extends ErroDaAplicacao {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    @Override
    public int status() {
        return 422;
    }

    @Override
    public String titulo() {
        return "Operação não permitida";
    }
}
