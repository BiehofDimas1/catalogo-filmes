package com.catalogo.filmes.service.pagamento;

import java.math.BigDecimal;

public interface GatewayDeCobranca {

    ResultadoPagamento cobrar(BigDecimal valor, String descricao);
}
