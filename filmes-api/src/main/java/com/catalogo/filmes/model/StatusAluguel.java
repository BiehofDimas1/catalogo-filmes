package com.catalogo.filmes.model;

/**
 * Situação de um aluguel. Um aluguel nasce {@link #ATIVO} e termina
 * {@link #EXPIRADO} (após a validade) ou {@link #CANCELADO} (pelo dono, dentro do prazo).
 */
public enum StatusAluguel {
    ATIVO,
    EXPIRADO,
    CANCELADO
}
