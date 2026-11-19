package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.service.aluguel.validacao.AluguelAindaAtivoValidador;
import com.catalogo.filmes.service.aluguel.validacao.DonoDoAluguelValidador;
import com.catalogo.filmes.service.aluguel.validacao.PrazoDeCancelamentoValidador;
import com.catalogo.filmes.service.aluguel.validacao.ValidadorCancelamento;
import com.catalogo.filmes.service.exception.AcessoNegadoException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import com.catalogo.filmes.service.pagamento.GatewayDeReembolso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CancelamentoAluguelServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-15T12:00:00Z");
    private static final BigDecimal VALOR = new BigDecimal("9.90");
    private static final AluguelProperties REGRAS =
            new AluguelProperties(VALOR, Duration.ofHours(48), Duration.ofHours(1), 5);

    private final AluguelRepository aluguelRepository = mock(AluguelRepository.class);
    private final GatewayDeReembolso reembolso = mock(GatewayDeReembolso.class);

    private Usuario cliente;
    private Filme filme;

    @BeforeEach
    void preparar() {
        cliente = usuario(1L, "Ana");
        filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", 10L);
        filme.setTitulo("Matriz");
        filme.setDuracaoMinutos(136);
        filme.setLicencasDisponiveis(2);
        when(reembolso.reembolsar(anyString(), any())).thenReturn("REE-SIM-1");
    }

    private static Usuario usuario(Long id, String nome) {
        Usuario usuario = new Usuario(nome, nome.toLowerCase() + "@teste.com", "hash", Perfil.CLIENTE, AGORA);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private CancelamentoAluguelService servicoEm(Instant instante) {
        List<ValidadorCancelamento> validadores = List.of(new DonoDoAluguelValidador(),
                new AluguelAindaAtivoValidador(), new PrazoDeCancelamentoValidador(REGRAS));
        return new CancelamentoAluguelService(aluguelRepository, validadores, reembolso, REGRAS,
                Clock.fixed(instante, ZoneOffset.UTC));
    }

    private Aluguel aluguelDe(Usuario dono) {
        Aluguel aluguel = Aluguel.novo(dono, filme, AGORA, Duration.ofHours(48), VALOR,
                "PAG-SIM-1", "codigo-1");
        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguel));
        return aluguel;
    }

    @Test
    void cancelarDentroDoPrazoReembolsaEDevolveALicenca() {
        aluguelDe(cliente);
        filme.retirarLicenca();

        AluguelResponse resposta = servicoEm(AGORA.plus(Duration.ofMinutes(30))).cancelar(1L, 5L);

        assertEquals(StatusAluguel.CANCELADO, resposta.status());
        assertEquals("REE-SIM-1", resposta.transacaoReembolso());
        assertFalse(resposta.cancelavel());
        assertEquals(2, filme.getLicencasDisponiveis());
        verify(reembolso).reembolsar("PAG-SIM-1", VALOR);
    }

    @Test
    void cancelarDepoisDeUmaHoraDeveFalhar() {
        Aluguel aluguel = aluguelDe(cliente);

        assertThrows(RegraNegocioException.class,
                () -> servicoEm(AGORA.plus(Duration.ofMinutes(61))).cancelar(1L, 5L));
        assertEquals(StatusAluguel.ATIVO, aluguel.getStatus());
        verify(reembolso, never()).reembolsar(anyString(), any());
    }

    @Test
    void clienteNaoCancelaAluguelDeOutroCliente() {
        Aluguel aluguelDoBruno = aluguelDe(usuario(2L, "Bruno"));

        assertThrows(AcessoNegadoException.class, () -> servicoEm(AGORA).cancelar(1L, 5L));
        assertEquals(StatusAluguel.ATIVO, aluguelDoBruno.getStatus());
    }

    @Test
    void aluguelExpiradoNaoPodeSerCancelado() {
        aluguelDe(cliente);

        assertThrows(RegraNegocioException.class,
                () -> servicoEm(AGORA.plus(Duration.ofHours(49))).cancelar(1L, 5L));
    }

    @Test
    void aluguelJaCanceladoNaoPodeSerCanceladoDeNovo() {
        Aluguel aluguel = aluguelDe(cliente);
        aluguel.cancelar(AGORA.plus(Duration.ofMinutes(5)), "REE-SIM-0");

        assertThrows(RegraNegocioException.class,
                () -> servicoEm(AGORA.plus(Duration.ofMinutes(10))).cancelar(1L, 5L));
    }

    @Test
    void aluguelInexistenteDeveFalhar() {
        when(aluguelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servicoEm(AGORA).cancelar(1L, 99L));
        verify(reembolso, never()).reembolsar(anyString(), any());
    }
}
