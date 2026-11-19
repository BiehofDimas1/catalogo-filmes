package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.AluguelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpiracaoAluguelServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-15T12:00:00Z");

    private final AluguelRepository aluguelRepository = mock(AluguelRepository.class);

    private Filme filme;
    private Aluguel aluguel;

    @BeforeEach
    void preparar() {
        Usuario ana = new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, AGORA);
        ReflectionTestUtils.setField(ana, "id", 1L);
        filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", 10L);
        filme.setTitulo("Matriz");
        filme.setDuracaoMinutos(136);
        filme.setLicencasDisponiveis(2);
        aluguel = Aluguel.novo(ana, filme, AGORA, Duration.ofHours(48), new BigDecimal("9.90"),
                "PAG-SIM-1", "codigo-1");
        filme.retirarLicenca();
        when(aluguelRepository.findById(7L)).thenReturn(Optional.of(aluguel));
    }

    private ExpiracaoAluguelService servicoEm(Instant instante) {
        return new ExpiracaoAluguelService(aluguelRepository, Clock.fixed(instante, ZoneOffset.UTC),
                mock(PlatformTransactionManager.class));
    }

    @Test
    void rotinaDeExpiracaoEncerraVencidosEDevolveLicencas() {
        Instant depois = AGORA.plus(Duration.ofHours(48));
        when(aluguelRepository.buscarIdsVencidos(StatusAluguel.ATIVO, depois)).thenReturn(List.of(7L));

        int expirados = servicoEm(depois).expirarVencidos();

        assertEquals(1, expirados);
        assertEquals(StatusAluguel.EXPIRADO, aluguel.getStatus());
        assertEquals(2, filme.getLicencasDisponiveis());
    }

    @Test
    void aluguelAindaNoPrazoNaoExpira() {
        Instant antes = AGORA.plus(Duration.ofHours(47));
        when(aluguelRepository.buscarIdsVencidos(StatusAluguel.ATIVO, antes)).thenReturn(List.of(7L));

        int expirados = servicoEm(antes).expirarVencidos();

        assertEquals(0, expirados);
        assertEquals(StatusAluguel.ATIVO, aluguel.getStatus());
        assertEquals(1, filme.getLicencasDisponiveis());
    }
}
