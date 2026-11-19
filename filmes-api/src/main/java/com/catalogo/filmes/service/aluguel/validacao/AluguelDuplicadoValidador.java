package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class AluguelDuplicadoValidador implements ValidadorAluguel {

    private final AluguelRepository aluguelRepository;

    @Override
    public void validar(PedidoAluguel pedido) {
        boolean jaAlugado = aluguelRepository.existsByUsuarioIdAndFilmeIdAndStatusAndDataExpiracaoAfter(
                pedido.usuarioId(), pedido.filme().getId(), StatusAluguel.ATIVO, pedido.agora());
        if (jaAlugado) {
            throw new ConflitoException("Você já tem um aluguel ativo deste filme.");
        }
    }
}
