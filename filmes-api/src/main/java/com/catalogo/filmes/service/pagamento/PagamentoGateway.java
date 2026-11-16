package com.catalogo.filmes.service.pagamento;

import java.math.BigDecimal;

/**
 * Fronteira de pagamento, como no projeto de referência. A implementação atual é simulada;
 * um provedor real poderia substituí-la sem mudar as regras do aluguel.
 */
public interface PagamentoGateway {

    ResultadoPagamento cobrar(BigDecimal valor, String descricao);

    /** Reembolsa integralmente uma cobrança e devolve o identificador do reembolso. */
    String reembolsar(String transacaoPagamento, BigDecimal valor);
}
