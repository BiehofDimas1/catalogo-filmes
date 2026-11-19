package com.catalogo.filmes.service.pagamento;

import java.math.BigDecimal;

public interface GatewayDeReembolso {

    String reembolsar(String transacaoPagamento, BigDecimal valor);
}
