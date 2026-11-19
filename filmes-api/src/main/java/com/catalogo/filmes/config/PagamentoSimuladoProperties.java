package com.catalogo.filmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "pagamento.simulado")
public record PagamentoSimuladoProperties(@DefaultValue("APROVADO") Resultado resultado) {

    public enum Resultado {
        APROVADO,
        RECUSADO
    }
}
