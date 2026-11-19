package com.catalogo.filmes.service.filme.validacao;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.model.Filme;

public record PedidoFilme(Filme atual, FilmeRequest dados) {

    public static PedidoFilme cadastro(FilmeRequest dados) {
        return new PedidoFilme(null, dados);
    }

    public static PedidoFilme edicao(Filme atual, FilmeRequest dados) {
        return new PedidoFilme(atual, dados);
    }

    public boolean ehEdicao() {
        return atual != null;
    }
}
