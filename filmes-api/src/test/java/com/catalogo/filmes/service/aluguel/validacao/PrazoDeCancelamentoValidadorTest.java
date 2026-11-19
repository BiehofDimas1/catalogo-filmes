package com.catalogo.filmes.service.aluguel.validacao;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrazoDeCancelamentoValidadorTest {

    @Test
    void prazoEhDescritoEmPortugues() {
        assertEquals("1 hora", PrazoDeCancelamentoValidador.descrever(Duration.ofHours(1)));
        assertEquals("2 horas", PrazoDeCancelamentoValidador.descrever(Duration.ofHours(2)));
        assertEquals("30 minutos", PrazoDeCancelamentoValidador.descrever(Duration.ofMinutes(30)));
    }
}
