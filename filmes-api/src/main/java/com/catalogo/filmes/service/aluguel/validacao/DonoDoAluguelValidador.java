package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.service.exception.AcessoNegadoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DonoDoAluguelValidador implements ValidadorCancelamento {

    @Override
    public void validar(PedidoCancelamento pedido) {
        if (!pedido.aluguel().pertenceA(pedido.usuarioId())) {
            throw new AcessoNegadoException("Você só pode cancelar os seus próprios aluguéis.");
        }
    }
}
