package com.catalogo.filmes.service.pagamento;

/** Resultado de uma cobrança: aprovada (com o identificador da transação) ou recusada. */
public record ResultadoPagamento(boolean aprovado, String transacao) {

    public static ResultadoPagamento aprovado(String transacao) {
        return new ResultadoPagamento(true, transacao);
    }

    public static ResultadoPagamento recusado() {
        return new ResultadoPagamento(false, null);
    }
}
