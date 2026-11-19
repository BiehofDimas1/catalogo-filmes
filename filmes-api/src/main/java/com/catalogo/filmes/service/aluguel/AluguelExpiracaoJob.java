package com.catalogo.filmes.service.aluguel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AluguelExpiracaoJob {

    private final ExpiracaoAluguelService expiracao;

    @Scheduled(fixedDelayString = "${aluguel.expiracao-intervalo:PT1M}",
            initialDelayString = "${aluguel.expiracao-intervalo:PT1M}")
    public void expirarAlugueisVencidos() {
        int expirados = expiracao.expirarVencidos();
        if (expirados > 0) {
            log.info("{} aluguel(is) expirado(s); licenças devolvidas aos filmes.", expirados);
        }
    }
}
