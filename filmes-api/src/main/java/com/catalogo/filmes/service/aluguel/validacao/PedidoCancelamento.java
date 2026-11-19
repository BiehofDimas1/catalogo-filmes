package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.model.Aluguel;

import java.time.Instant;

public record PedidoCancelamento(Long usuarioId, Aluguel aluguel, Instant agora) {
}
