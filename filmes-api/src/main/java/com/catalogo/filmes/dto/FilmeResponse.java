package com.catalogo.filmes.dto;

import com.catalogo.filmes.model.Filme;

public record FilmeResponse(Long id, String titulo, Integer duracaoMinutos, int licencasDisponiveis, boolean ativo) {

    public static FilmeResponse de(Filme filme) {
        return new FilmeResponse(filme.getId(), filme.getTitulo(), filme.getDuracaoMinutos(),
                filme.getLicencasDisponiveis(), filme.isAtivo());
    }
}
