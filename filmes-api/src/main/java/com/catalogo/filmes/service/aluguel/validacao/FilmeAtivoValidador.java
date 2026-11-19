package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.service.exception.RegraNegocioException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class FilmeAtivoValidador implements ValidadorAluguel {

    @Override
    public void validar(PedidoAluguel pedido) {
        if (!pedido.filme().isAtivo()) {
            throw new RegraNegocioException("Este filme está inativo e não pode ser alugado.");
        }
    }
}
