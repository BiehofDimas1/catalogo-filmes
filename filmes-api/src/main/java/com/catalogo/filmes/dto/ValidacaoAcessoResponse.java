package com.catalogo.filmes.dto;

import com.catalogo.filmes.model.StatusAluguel;

import java.time.Instant;

/** Resultado da validação de um código de acesso (equivalente à validação de entrada do ingresso). */
public record ValidacaoAcessoResponse(
        boolean valido,
        String mensagem,
        StatusAluguel status,
        String filmeTitulo,
        String usuarioNome,
        Instant dataExpiracao) {
}
