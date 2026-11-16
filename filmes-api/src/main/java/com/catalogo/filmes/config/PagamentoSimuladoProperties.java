package com.catalogo.filmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Resultado do pagamento simulado ({@code pagamento.simulado.resultado}).
 * Só o servidor escolhe o resultado; o cliente nunca informa se o pagamento passa.
 */
@ConfigurationProperties(prefix = "pagamento.simulado")
public record PagamentoSimuladoProperties(@DefaultValue("APROVADO") Resultado resultado) {

    public enum Resultado {
        APROVADO,
        RECUSADO
    }
}
