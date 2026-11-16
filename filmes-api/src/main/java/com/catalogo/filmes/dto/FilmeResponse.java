package com.catalogo.filmes.dto;

import com.catalogo.filmes.model.Filme;

/**
 * {@code versao} muda a cada alteração do filme, inclusive quando um aluguel retira ou devolve uma licença.
 * O formulário de edição devolve essa versão no {@code PUT}.
 */
public record FilmeResponse(Long id, String titulo, Integer duracaoMinutos, int licencasDisponiveis, boolean ativo,
                            Long versao) {

    public static FilmeResponse de(Filme filme) {
        return new FilmeResponse(filme.getId(), filme.getTitulo(), filme.getDuracaoMinutos(),
                filme.getLicencasDisponiveis(), filme.isAtivo(), filme.getVersao());
    }
}
