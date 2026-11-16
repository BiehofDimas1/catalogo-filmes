package com.catalogo.filmes.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AluguelTest {

    private static final Instant INICIO = Instant.parse("2026-11-15T12:00:00Z");
    private static final Duration PRAZO_CANCELAMENTO = Duration.ofHours(1);

    private Aluguel novoAluguel() {
        Usuario usuario = new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, INICIO);
        Filme filme = new Filme();
        filme.setTitulo("Matriz");
        filme.setDuracaoMinutos(136);
        filme.setLicencasDisponiveis(1);
        return Aluguel.novo(usuario, filme, INICIO, Duration.ofHours(48), new BigDecimal("9.90"), "PAG-1", "codigo-1");
    }

    @Test
    void aluguelNovoFicaAtivoPor48Horas() {
        Aluguel aluguel = novoAluguel();

        assertEquals(INICIO.plus(Duration.ofHours(48)), aluguel.getDataExpiracao());
        assertEquals(StatusAluguel.ATIVO, aluguel.statusEm(INICIO));
        assertEquals(StatusAluguel.ATIVO, aluguel.statusEm(INICIO.plus(Duration.ofMinutes(48 * 60 - 1))));
    }

    @Test
    void aluguelVencidoContaComoExpirado() {
        Aluguel aluguel = novoAluguel();

        assertEquals(StatusAluguel.EXPIRADO, aluguel.statusEm(INICIO.plus(Duration.ofHours(48))));
        assertTrue(aluguel.estaVencido(INICIO.plus(Duration.ofHours(50))));
    }

    @Test
    void cancelamentoSoDentroDeUmaHora() {
        Aluguel aluguel = novoAluguel();

        assertTrue(aluguel.podeSerCanceladoEm(INICIO.plus(Duration.ofMinutes(59)), PRAZO_CANCELAMENTO));
        assertTrue(aluguel.podeSerCanceladoEm(INICIO.plus(Duration.ofMinutes(60)), PRAZO_CANCELAMENTO));
        assertFalse(aluguel.podeSerCanceladoEm(INICIO.plus(Duration.ofMinutes(61)), PRAZO_CANCELAMENTO));
    }

    @Test
    void aluguelCanceladoNaoPodeSerCanceladoDeNovo() {
        Aluguel aluguel = novoAluguel();
        aluguel.cancelar(INICIO.plus(Duration.ofMinutes(10)), "REE-1");

        assertEquals(StatusAluguel.CANCELADO, aluguel.statusEm(INICIO.plus(Duration.ofMinutes(11))));
        assertFalse(aluguel.podeSerCanceladoEm(INICIO.plus(Duration.ofMinutes(11)), PRAZO_CANCELAMENTO));
        assertEquals("REE-1", aluguel.getTransacaoReembolso());
    }

    @Test
    void tituloDoFilmeEhNormalizadoParaComparar() {
        assertEquals("o poderoso chefão", Filme.normalizarTitulo("  O   Poderoso CHEFÃO "));
    }
}
