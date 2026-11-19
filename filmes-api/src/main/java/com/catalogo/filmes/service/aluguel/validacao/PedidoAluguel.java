package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.model.Filme;

import java.time.Instant;

public record PedidoAluguel(Long usuarioId, Filme filme, Instant agora) {
}
