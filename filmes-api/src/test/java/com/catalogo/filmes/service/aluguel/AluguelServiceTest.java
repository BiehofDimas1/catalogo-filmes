package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.service.aluguel.validacao.AluguelDuplicadoValidador;
import com.catalogo.filmes.service.aluguel.validacao.FilmeAtivoValidador;
import com.catalogo.filmes.service.aluguel.validacao.LicencaDisponivelValidador;
import com.catalogo.filmes.service.aluguel.validacao.LimiteAlugueisAtivosValidador;
import com.catalogo.filmes.service.aluguel.validacao.ValidadorAluguel;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import com.catalogo.filmes.service.pagamento.GatewayDeCobranca;
import com.catalogo.filmes.service.pagamento.ResultadoPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AluguelServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-15T12:00:00Z");
    private static final BigDecimal VALOR = new BigDecimal("9.90");
    private static final AluguelProperties REGRAS =
            new AluguelProperties(VALOR, Duration.ofHours(48), Duration.ofHours(1), 5);

    private final AluguelRepository aluguelRepository = mock(AluguelRepository.class);
    private final FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final GatewayDeCobranca cobranca = mock(GatewayDeCobranca.class);

    private Filme filme;

    @BeforeEach
    void preparar() {
        filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", 10L);
        filme.setTitulo("Matriz");
        filme.setDuracaoMinutos(136);
        filme.setLicencasDisponiveis(2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "Ana")));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(aluguelRepository.findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(cobranca.cobrar(any(), anyString())).thenReturn(ResultadoPagamento.aprovado("PAG-SIM-1"));
        when(aluguelRepository.saveAndFlush(any(Aluguel.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private static Usuario usuario(Long id, String nome) {
        Usuario usuario = new Usuario(nome, nome.toLowerCase() + "@teste.com", "hash", Perfil.CLIENTE, AGORA);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private List<ValidadorAluguel> validadoresDoSistema() {
        return List.of(
                new FilmeAtivoValidador(),
                new AluguelDuplicadoValidador(aluguelRepository),
                new LimiteAlugueisAtivosValidador(aluguelRepository, REGRAS),
                new LicencaDisponivelValidador());
    }

    private AluguelService servico(List<ValidadorAluguel> validadores) {
        Clock relogio = Clock.fixed(AGORA, ZoneOffset.UTC);
        PlatformTransactionManager transacoes = mock(PlatformTransactionManager.class);
        ExpiracaoAluguelService expiracao =
                new ExpiracaoAluguelService(aluguelRepository, relogio, transacoes);
        return new AluguelService(aluguelRepository, filmeRepository, usuarioRepository, expiracao,
                validadores, cobranca, REGRAS, relogio);
    }

    private AluguelService servico() {
        return servico(validadoresDoSistema());
    }

    @Test
    void aluguelValidoCobraOValorEConsomeUmaLicenca() {
        AluguelResponse resposta = servico().alugar(1L, 10L);

        assertEquals(StatusAluguel.ATIVO, resposta.status());
        assertEquals(AGORA.plus(Duration.ofHours(48)), resposta.dataExpiracao());
        assertEquals(VALOR, resposta.valor());
        assertEquals("PAG-SIM-1", resposta.transacaoPagamento());
        assertTrue(resposta.cancelavel());
        assertNotNull(resposta.codigoAcesso());
        assertEquals(1, filme.getLicencasDisponiveis());
        verify(cobranca).cobrar(eq(VALOR), anyString());
    }

    @Test
    void filmeInexistenteDeveFalhar() {
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico().alugar(1L, 99L));
        verify(cobranca, never()).cobrar(any(), anyString());
    }

    @Test
    void filmeInativoNaoPodeSerAlugado() {
        filme.inativar();

        assertThrows(RegraNegocioException.class, () -> servico().alugar(1L, 10L));
        verify(cobranca, never()).cobrar(any(), anyString());
    }

    @Test
    void naoPermiteDoisAlugueisAtivosDoMesmoFilme() {
        when(aluguelRepository.existsByUsuarioIdAndFilmeIdAndStatusAndDataExpiracaoAfter(
                1L, 10L, StatusAluguel.ATIVO, AGORA)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> servico().alugar(1L, 10L));
        assertEquals(2, filme.getLicencasDisponiveis());
    }

    @Test
    void naoPermiteMaisDeCincoAlugueisAtivos() {
        when(aluguelRepository.countByUsuarioIdAndStatusAndDataExpiracaoAfter(1L, StatusAluguel.ATIVO, AGORA))
                .thenReturn(5L);

        RegraNegocioException erro =
                assertThrows(RegraNegocioException.class, () -> servico().alugar(1L, 10L));
        assertTrue(erro.getMessage().contains("limite de 5"));
    }

    @Test
    void semLicencaDisponivelNaoAluga() {
        filme.setLicencasDisponiveis(0);

        assertThrows(RegraNegocioException.class, () -> servico().alugar(1L, 10L));
        verify(cobranca, never()).cobrar(any(), anyString());
    }

    @Test
    void licencaDeAluguelVencidoVoltaAntesDeChecarADisponibilidade() {
        filme.setLicencasDisponiveis(0);
        Aluguel vencido = Aluguel.novo(usuario(2L, "Bruno"), filme, AGORA.minus(Duration.ofHours(49)),
                Duration.ofHours(48), VALOR, "PAG-SIM-0", "codigo-0");
        when(aluguelRepository.findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(
                10L, StatusAluguel.ATIVO, AGORA)).thenReturn(List.of(vencido));

        AluguelResponse resposta = servico().alugar(1L, 10L);

        assertEquals(StatusAluguel.ATIVO, resposta.status());
        assertEquals(StatusAluguel.EXPIRADO, vencido.getStatus());
        assertEquals(0, filme.getLicencasDisponiveis());
    }

    @Test
    void pagamentoRecusadoNaoCriaAluguelNemConsomeLicenca() {
        when(cobranca.cobrar(any(), anyString())).thenReturn(ResultadoPagamento.recusado());

        assertThrows(RegraNegocioException.class, () -> servico().alugar(1L, 10L));
        assertEquals(2, filme.getLicencasDisponiveis());
        verify(aluguelRepository, never()).saveAndFlush(any());
    }

    @Test
    void regraNovaEntraSemMudarOServico() {
        List<ValidadorAluguel> validadores = new ArrayList<>(validadoresDoSistema());
        validadores.add(pedido -> {
            throw new RegraNegocioException("Aluguéis suspensos para manutenção.");
        });

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico(validadores).alugar(1L, 10L));

        assertEquals("Aluguéis suspensos para manutenção.", erro.getMessage());
        verify(cobranca, never()).cobrar(any(), anyString());
    }
}
