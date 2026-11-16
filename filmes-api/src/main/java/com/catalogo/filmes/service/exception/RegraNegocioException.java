package com.catalogo.filmes.service.exception;

/** A operação viola uma regra do aluguel (limite, disponibilidade, prazo, pagamento). Vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
