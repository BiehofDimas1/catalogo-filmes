package com.catalogo.filmes.dto;

import java.math.BigDecimal;

public record RegrasAluguelResponse(
        BigDecimal valor,
        long duracaoHoras,
        long prazoCancelamentoMinutos,
        int limiteAtivos) {
}
