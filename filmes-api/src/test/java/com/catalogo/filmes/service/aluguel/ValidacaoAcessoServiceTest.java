package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.dto.ValidacaoAcessoResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidacaoAcessoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-15T12:00:00Z");

    private final AluguelRepository aluguelRepository = mock(AluguelRepository.class);

    private ValidacaoAcessoService servicoEm(Instant instante) {
        return new ValidacaoAcessoService(aluguelRepository, Clock.fixed(instante, ZoneOffset.UTC));
    }

    private void cadastrarAluguelDaAna() {
        Usuario ana = new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, AGORA);
        ReflectionTestUtils.setField(ana, "id", 1L);
        Filme filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", 10L);
        filme.setTitulo("Matriz");
        filme.setDuracaoMinutos(136);
        filme.setLicencasDisponiveis(1);
        Aluguel aluguel = Aluguel.novo(ana, filme, AGORA, Duration.ofHours(48), new BigDecimal("9.90"),
                "PAG-SIM-1", "codigo-1");
        when(aluguelRepository.findByCodigoAcesso("codigo-1")).thenReturn(Optional.of(aluguel));
    }

    @Test
    void validarCodigoDeAluguelAtivoLiberaSemConsumirOCodigo() {
        cadastrarAluguelDaAna();
        ValidacaoAcessoService service = servicoEm(AGORA.plus(Duration.ofHours(1)));

        ValidacaoAcessoResponse primeira = service.validar(" codigo-1 ");
        ValidacaoAcessoResponse segunda = service.validar("codigo-1");

        assertTrue(primeira.valido());
        assertTrue(segunda.valido());
        assertEquals("Matriz", primeira.filmeTitulo());
        assertEquals("Ana", primeira.usuarioNome());
    }

    @Test
    void validarCodigoDeAluguelExpiradoNegaOAcesso() {
        cadastrarAluguelDaAna();

        ValidacaoAcessoResponse resposta = servicoEm(AGORA.plus(Duration.ofHours(48))).validar("codigo-1");

        assertFalse(resposta.valido());
        assertEquals(StatusAluguel.EXPIRADO, resposta.status());
    }

    @Test
    void validarCodigoInexistenteDeveFalhar() {
        when(aluguelRepository.findByCodigoAcesso("nao-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servicoEm(AGORA).validar("nao-existe"));
    }
}
