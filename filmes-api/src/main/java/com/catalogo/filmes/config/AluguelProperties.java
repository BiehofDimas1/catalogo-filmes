package com.catalogo.filmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;
import java.time.Duration;

@ConfigurationProperties(prefix = "aluguel")
public record AluguelProperties(
        @DefaultValue("9.90") BigDecimal valor,
        @DefaultValue("48h") Duration duracao,
        @DefaultValue("1h") Duration prazoCancelamento,
        @DefaultValue("5") int limiteAtivos) {
}
