package com.catalogo.filmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Regras do aluguel, lidas de {@code aluguel.*} no application.properties.
 *
 * @param valor             preço fixo de cada aluguel
 * @param duracao           validade do aluguel
 * @param prazoCancelamento tempo, a partir do aluguel, em que o dono ainda pode cancelar
 * @param limiteAtivos      máximo de aluguéis ativos por usuário
 */
@ConfigurationProperties(prefix = "aluguel")
public record AluguelProperties(
        @DefaultValue("9.90") BigDecimal valor,
        @DefaultValue("48h") Duration duracao,
        @DefaultValue("1h") Duration prazoCancelamento,
        @DefaultValue("5") int limiteAtivos) {
}
