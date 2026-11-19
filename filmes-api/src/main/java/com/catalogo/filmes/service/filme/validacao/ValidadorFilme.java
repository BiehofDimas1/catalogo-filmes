package com.catalogo.filmes.service.filme.validacao;

@FunctionalInterface
public interface ValidadorFilme {

    void validar(PedidoFilme pedido);
}
