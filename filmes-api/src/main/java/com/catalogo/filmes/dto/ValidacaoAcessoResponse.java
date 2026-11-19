package com.catalogo.filmes.dto;

import com.catalogo.filmes.model.StatusAluguel;

import java.time.Instant;

public record ValidacaoAcessoResponse(
        boolean valido,
        String mensagem,
        StatusAluguel status,
        String filmeTitulo,
        String usuarioNome,
        Instant dataExpiracao) {
}
