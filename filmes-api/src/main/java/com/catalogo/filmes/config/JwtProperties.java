package com.catalogo.filmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuração do JWT ({@code jwt.*}).
 *
 * @param secret   segredo HMAC; vazio faz a API gerar uma chave aleatória a cada início
 * @param validade tempo de vida do token
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, @DefaultValue("2h") Duration validade) {
}
