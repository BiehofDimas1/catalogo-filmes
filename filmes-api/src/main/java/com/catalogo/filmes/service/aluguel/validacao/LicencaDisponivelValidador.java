package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.service.exception.RegraNegocioException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class LicencaDisponivelValidador implements ValidadorAluguel {

    @Override
    public void validar(PedidoAluguel pedido) {
        if (!pedido.filme().temLicencaDisponivel()) {
            throw new RegraNegocioException("Não há licenças disponíveis para este filme no momento.");
        }
    }
}
