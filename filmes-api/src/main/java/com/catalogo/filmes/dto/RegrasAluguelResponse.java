package com.catalogo.filmes.dto;

import java.math.BigDecimal;

/** Regras atuais do aluguel, para a interface exibir preço, validade e limites. */
public record RegrasAluguelResponse(
        BigDecimal valor,
        long duracaoHoras,
        long prazoCancelamentoMinutos,
        int limiteAtivos) {
}
