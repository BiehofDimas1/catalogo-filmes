package com.catalogo.filmes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RelogioConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
