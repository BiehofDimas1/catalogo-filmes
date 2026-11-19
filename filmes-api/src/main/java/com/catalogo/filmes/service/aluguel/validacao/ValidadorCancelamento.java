package com.catalogo.filmes.service.aluguel.validacao;

@FunctionalInterface
public interface ValidadorCancelamento {

    void validar(PedidoCancelamento pedido);
}
