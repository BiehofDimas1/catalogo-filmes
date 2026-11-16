package com.catalogo.filmes.model;

/**
 * Perfis de acesso. O cadastro público sempre cria {@link #CLIENTE};
 * {@link #ADMIN} só é concedido manualmente pelo console do H2.
 */
public enum Perfil {
    ADMIN,
    CLIENTE
}
