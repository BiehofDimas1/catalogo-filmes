package com.catalogo.filmes.dto;

import java.time.Instant;

/** Resposta do login: o token JWT e os dados do usuário, para a interface montar o menu. */
public record LoginResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) {
}
