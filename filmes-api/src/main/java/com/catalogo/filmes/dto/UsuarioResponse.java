package com.catalogo.filmes.dto;

import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
