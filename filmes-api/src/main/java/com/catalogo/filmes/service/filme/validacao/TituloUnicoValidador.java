package com.catalogo.filmes.service.filme.validacao;

import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class TituloUnicoValidador implements ValidadorFilme {

    private final FilmeRepository repository;

    @Override
    public void validar(PedidoFilme pedido) {
        if (tituloJaExiste(pedido)) {
            throw new ConflitoException(
                    "Já existe um filme com o título \"" + pedido.dados().titulo().trim() + "\".");
        }
    }

    private boolean tituloJaExiste(PedidoFilme pedido) {
        String titulo = Filme.normalizarTitulo(pedido.dados().titulo());
        return pedido.ehEdicao()
                ? repository.existsByTituloNormalizadoAndIdNot(titulo, pedido.atual().getId())
                : repository.existsByTituloNormalizado(titulo);
    }
}
