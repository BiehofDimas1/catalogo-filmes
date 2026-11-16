package com.catalogo.filmes.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Rotina periódica que encerra os aluguéis vencidos e devolve as licenças aos filmes. */
@Component
@RequiredArgsConstructor
public class AluguelExpiracaoJob {

    private static final Logger log = LoggerFactory.getLogger(AluguelExpiracaoJob.class);

    private final AluguelService aluguelService;

    @Scheduled(fixedDelayString = "${aluguel.expiracao-intervalo:PT1M}",
            initialDelayString = "${aluguel.expiracao-intervalo:PT1M}")
    public void expirarAlugueisVencidos() {
        int expirados = aluguelService.expirarVencidos();
        if (expirados > 0) {
            log.info("{} aluguel(is) expirado(s); licenças devolvidas aos filmes.", expirados);
        }
    }
}
