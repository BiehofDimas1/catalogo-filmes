package com.catalogo.filmes.service.aluguel.validacao;

@FunctionalInterface
public interface ValidadorAluguel {

    void validar(PedidoAluguel pedido);
}
