package com.catalogo.filmes.security;

import com.catalogo.filmes.model.Usuario;

public interface GeradorDeToken {

    TokenGerado gerar(Usuario usuario);
}
