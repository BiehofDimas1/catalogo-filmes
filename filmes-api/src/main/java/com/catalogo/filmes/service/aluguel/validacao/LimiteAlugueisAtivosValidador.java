package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@RequiredArgsConstructor
public class LimiteAlugueisAtivosValidador implements ValidadorAluguel {

    private final AluguelRepository aluguelRepository;
    private final AluguelProperties regras;

    @Override
    public void validar(PedidoAluguel pedido) {
        long ativos = aluguelRepository.countByUsuarioIdAndStatusAndDataExpiracaoAfter(
                pedido.usuarioId(), StatusAluguel.ATIVO, pedido.agora());
        if (ativos >= regras.limiteAtivos()) {
            throw new RegraNegocioException("Você atingiu o limite de " + regras.limiteAtivos()
                    + " aluguéis ativos. Aguarde um deles expirar ou cancele um aluguel.");
        }
    }
}
