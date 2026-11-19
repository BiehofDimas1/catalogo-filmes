package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AluguelAindaAtivoValidador implements ValidadorCancelamento {

    @Override
    public void validar(PedidoCancelamento pedido) {
        StatusAluguel status = pedido.aluguel().statusEm(pedido.agora());
        if (status == StatusAluguel.CANCELADO) {
            throw new RegraNegocioException("Este aluguel já foi cancelado.");
        }
        if (status == StatusAluguel.EXPIRADO) {
            throw new RegraNegocioException("Este aluguel já expirou e não pode mais ser cancelado.");
        }
    }
}
