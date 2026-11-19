package com.catalogo.filmes.service.pagamento;

public record ResultadoPagamento(boolean aprovado, String transacao) {

    public static ResultadoPagamento aprovado(String transacao) {
        return new ResultadoPagamento(true, transacao);
    }

    public static ResultadoPagamento recusado() {
        return new ResultadoPagamento(false, null);
    }
}
