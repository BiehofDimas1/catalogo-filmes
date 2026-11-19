package com.catalogo.filmes.service.filme.validacao;

import com.catalogo.filmes.service.exception.VersaoDesatualizadaException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Order(1)
public class VersaoAtualValidador implements ValidadorFilme {

    private static final String MENSAGEM = "Este filme mudou depois que você abriu o formulário "
            + "(por exemplo, por um aluguel). "
            + "Recarregue a página para ver os dados atuais e salve de novo.";

    @Override
    public void validar(PedidoFilme pedido) {
        if (pedido.ehEdicao() && mudouDesdeQueFoiAberto(pedido)) {
            throw new VersaoDesatualizadaException(MENSAGEM);
        }
    }

    private static boolean mudouDesdeQueFoiAberto(PedidoFilme pedido) {
        return !Objects.equals(pedido.dados().versao(), pedido.atual().getVersao());
    }
}
