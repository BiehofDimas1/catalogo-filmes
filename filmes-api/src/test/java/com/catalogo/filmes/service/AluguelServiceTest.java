package com.catalogo.filmes.service;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.dto.ValidacaoAcessoResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.service.exception.AcessoNegadoException;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import com.catalogo.filmes.service.pagamento.PagamentoGateway;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Regras do fluxo principal (aluguel), com relógio fixo para testar prazos. */
class AluguelServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-15T12:00:00Z");
    private static final BigDecimal VALOR = new BigDecimal("9.90");
    private static final AluguelProperties REGRAS =
            new AluguelProperties(VALOR, Duration.ofHours(48), Duration.ofHours(1), 5);

    private final AluguelRepository aluguelRepository = mock(AluguelRepository.class);
    private final FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PagamentoGateway pagamento = mock(PagamentoGateway.class);

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

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(aluguelRepository.findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(pagamento.cobrar(any(), anyString())).thenReturn(ResultadoPagamento.aprovado("PAG-SIM-1"));
        when(pagamento.reembolsar(anyString(), any())).thenReturn("REE-SIM-1");
        when(aluguelRepository.saveAndFlush(any(Aluguel.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static Usuario usuario(Long id, String nome) {
        Usuario usuario = new Usuario(nome, nome.toLowerCase() + "@teste.com", "hash", Perfil.CLIENTE, AGORA);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private AluguelService servicoEm(Instant instante) {
        return new AluguelService(aluguelRepository, filmeRepository, usuarioRepository, pagamento, REGRAS,
                Clock.fixed(instante, ZoneOffset.UTC), mock(PlatformTransactionManager.class));
    }

    private Aluguel aluguelDe(Usuario dono, Instant inicio) {
        return Aluguel.novo(dono, filme, inicio, Duration.ofHours(48), VALOR, "PAG-SIM-1", "codigo-1");
    }

    @Test
    void aluguelValidoCobraOValorEConsomeUmaLicenca() {
        AluguelResponse resposta = servicoEm(AGORA).alugar(1L, 10L);

        assertEquals(StatusAluguel.ATIVO, resposta.status());
        assertEquals(AGORA.plus(Duration.ofHours(48)), resposta.dataExpiracao());
        assertEquals(VALOR, resposta.valor());
        assertEquals("PAG-SIM-1", resposta.transacaoPagamento());
        assertTrue(resposta.cancelavel());
        assertNotNull(resposta.codigoAcesso());
        assertEquals(1, filme.getLicencasDisponiveis());
        verify(pagamento).cobrar(eq(VALOR), anyString());
    }

    @Test
    void filmeInativoNaoPodeSerAlugado() {
        filme.inativar();

        assertThrows(RegraNegocioException.class, () -> servicoEm(AGORA).alugar(1L, 10L));
        verify(pagamento, never()).cobrar(any(), anyString());
    }

    @Test
    void naoPermiteDoisAlugueisAtivosDoMesmoFilme() {
        when(aluguelRepository.existsByUsuarioIdAndFilmeIdAndStatusAndDataExpiracaoAfter(
                1L, 10L, StatusAluguel.ATIVO, AGORA)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> servicoEm(AGORA).alugar(1L, 10L));
        assertEquals(2, filme.getLicencasDisponiveis());
    }

    @Test
    void naoPermiteMaisDeCincoAlugueisAtivos() {
        when(aluguelRepository.countByUsuarioIdAndStatusAndDataExpiracaoAfter(1L, StatusAluguel.ATIVO, AGORA))
                .thenReturn(5L);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> servicoEm(AGORA).alugar(1L, 10L));
        assertTrue(erro.getMessage().contains("limite de 5"));
    }

    @Test
    void semLicencaDisponivelNaoAluga() {
        filme.setLicencasDisponiveis(0);

        assertThrows(RegraNegocioException.class, () -> servicoEm(AGORA).alugar(1L, 10L));
        verify(pagamento, never()).cobrar(any(), anyString());
    }

    @Test
    void licencaDeAluguelVencidoVoltaAntesDeChecarADisponibilidade() {
        filme.setLicencasDisponiveis(0);
        Aluguel vencido = aluguelDe(usuario(2L, "Bruno"), AGORA.minus(Duration.ofHours(49)));
        when(aluguelRepository.findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(10L, StatusAluguel.ATIVO, AGORA))
                .thenReturn(List.of(vencido));

        AluguelResponse resposta = servicoEm(AGORA).alugar(1L, 10L);

        assertEquals(StatusAluguel.ATIVO, resposta.status());
        assertEquals(StatusAluguel.EXPIRADO, vencido.getStatus());
        assertEquals(0, filme.getLicencasDisponiveis());
    }

    @Test
    void pagamentoRecusadoNaoCriaAluguelNemConsomeLicenca() {
        when(pagamento.cobrar(any(), anyString())).thenReturn(ResultadoPagamento.recusado());

        assertThrows(RegraNegocioException.class, () -> servicoEm(AGORA).alugar(1L, 10L));
        assertEquals(2, filme.getLicencasDisponiveis());
        verify(aluguelRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelarDentroDoPrazoReembolsaEDevolveALicenca() {
        Aluguel aluguel = aluguelDe(cliente, AGORA);
        filme.retirarLicenca();
        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguel));

        AluguelResponse resposta = servicoEm(AGORA.plus(Duration.ofMinutes(30))).cancelar(1L, 5L);

        assertEquals(StatusAluguel.CANCELADO, resposta.status());
        assertEquals("REE-SIM-1", resposta.transacaoReembolso());
        assertFalse(resposta.cancelavel());
        assertEquals(2, filme.getLicencasDisponiveis());
        verify(pagamento).reembolsar("PAG-SIM-1", VALOR);
    }

    @Test
    void cancelarDepoisDeUmaHoraDeveFalhar() {
        Aluguel aluguel = aluguelDe(cliente, AGORA);
        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguel));

        assertThrows(RegraNegocioException.class,
                () -> servicoEm(AGORA.plus(Duration.ofMinutes(61))).cancelar(1L, 5L));
        assertEquals(StatusAluguel.ATIVO, aluguel.getStatus());
        verify(pagamento, never()).reembolsar(anyString(), any());
    }

    @Test
    void clienteNaoCancelaAluguelDeOutroCliente() {
        Aluguel aluguelDoBruno = aluguelDe(usuario(2L, "Bruno"), AGORA);
        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguelDoBruno));

        assertThrows(AcessoNegadoException.class, () -> servicoEm(AGORA).cancelar(1L, 5L));
        assertEquals(StatusAluguel.ATIVO, aluguelDoBruno.getStatus());
    }

    @Test
    void aluguelExpiradoNaoPodeSerCancelado() {
        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguelDe(cliente, AGORA)));

        assertThrows(RegraNegocioException.class,
                () -> servicoEm(AGORA.plus(Duration.ofHours(49))).cancelar(1L, 5L));
    }

    @Test
    void aluguelJaCanceladoNaoPodeSerCanceladoDeNovo() {
        Aluguel aluguel = aluguelDe(cliente, AGORA);
        aluguel.cancelar(AGORA.plus(Duration.ofMinutes(5)), "REE-SIM-0");
        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguel));

        assertThrows(RegraNegocioException.class,
                () -> servicoEm(AGORA.plus(Duration.ofMinutes(10))).cancelar(1L, 5L));
    }

    @Test
    void validarCodigoDeAluguelAtivoLiberaSemConsumirOCodigo() {
        when(aluguelRepository.findByCodigoAcesso("codigo-1")).thenReturn(Optional.of(aluguelDe(cliente, AGORA)));
        AluguelService service = servicoEm(AGORA.plus(Duration.ofHours(1)));

        ValidacaoAcessoResponse primeira = service.validarAcesso(" codigo-1 ");
        ValidacaoAcessoResponse segunda = service.validarAcesso("codigo-1");

        assertTrue(primeira.valido());
        assertTrue(segunda.valido());
        assertEquals("Matriz", primeira.filmeTitulo());
        assertEquals("Ana", primeira.usuarioNome());
    }

    @Test
    void validarCodigoDeAluguelExpiradoNegaOAcesso() {
        when(aluguelRepository.findByCodigoAcesso("codigo-1")).thenReturn(Optional.of(aluguelDe(cliente, AGORA)));

        ValidacaoAcessoResponse resposta = servicoEm(AGORA.plus(Duration.ofHours(48))).validarAcesso("codigo-1");

        assertFalse(resposta.valido());
        assertEquals(StatusAluguel.EXPIRADO, resposta.status());
    }

    @Test
    void validarCodigoInexistenteDeveFalhar() {
        when(aluguelRepository.findByCodigoAcesso("nao-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servicoEm(AGORA).validarAcesso("nao-existe"));
    }

    @Test
    void rotinaDeExpiracaoEncerraVencidosEDevolveLicencas() {
        Instant depois = AGORA.plus(Duration.ofHours(48));
        Aluguel aluguel = aluguelDe(cliente, AGORA);
        filme.retirarLicenca();
        when(aluguelRepository.buscarIdsVencidos(StatusAluguel.ATIVO, depois)).thenReturn(List.of(7L));
        when(aluguelRepository.findById(7L)).thenReturn(Optional.of(aluguel));

        int expirados = servicoEm(depois).expirarVencidos();

        assertEquals(1, expirados);
        assertEquals(StatusAluguel.EXPIRADO, aluguel.getStatus());
        assertEquals(2, filme.getLicencasDisponiveis());
    }

    @Test
    void prazoEhDescritoEmPortugues() {
        assertEquals("1 hora", AluguelService.descrever(Duration.ofHours(1)));
        assertEquals("2 horas", AluguelService.descrever(Duration.ofHours(2)));
        assertEquals("30 minutos", AluguelService.descrever(Duration.ofMinutes(30)));
    }
}
