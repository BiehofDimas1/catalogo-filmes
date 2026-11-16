package com.catalogo.filmes.service;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.dto.RegrasAluguelResponse;
import com.catalogo.filmes.dto.ValidacaoAcessoResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fluxo principal: aluguel de filmes (equivalente ao fluxo de pedidos do projeto de referência).
 * Controla disponibilidade (licenças), status (ativo, expirado, cancelado) e o histórico do cliente.
 */
@Service
public class AluguelService {

    private static final Logger log = LoggerFactory.getLogger(AluguelService.class);

    private final AluguelRepository aluguelRepository;
    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagamentoGateway pagamentoGateway;
    private final AluguelProperties regras;
    private final Clock clock;
    private final TransactionTemplate transacao;

    public AluguelService(AluguelRepository aluguelRepository, FilmeRepository filmeRepository,
                          UsuarioRepository usuarioRepository, PagamentoGateway pagamentoGateway,
                          AluguelProperties regras, Clock clock, PlatformTransactionManager transactionManager) {
        this.aluguelRepository = aluguelRepository;
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagamentoGateway = pagamentoGateway;
        this.regras = regras;
        this.clock = clock;
        this.transacao = new TransactionTemplate(transactionManager);
    }

    public RegrasAluguelResponse regras() {
        return new RegrasAluguelResponse(regras.valor(), regras.duracao().toHours(),
                regras.prazoCancelamento().toMinutes(), regras.limiteAtivos());
    }

    @Transactional
    public AluguelResponse alugar(Long usuarioId, Long filmeId) {
        Instant agora = agora();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));

        if (!filme.isAtivo()) {
            throw new RegraNegocioException("Este filme está inativo e não pode ser alugado.");
        }
        if (aluguelRepository.existsByUsuarioIdAndFilmeIdAndStatusAndDataExpiracaoAfter(
                usuarioId, filmeId, StatusAluguel.ATIVO, agora)) {
            throw new ConflitoException("Você já tem um aluguel ativo deste filme.");
        }
        long ativos = aluguelRepository.countByUsuarioIdAndStatusAndDataExpiracaoAfter(
                usuarioId, StatusAluguel.ATIVO, agora);
        if (ativos >= regras.limiteAtivos()) {
            throw new RegraNegocioException("Você atingiu o limite de " + regras.limiteAtivos()
                    + " aluguéis ativos. Aguarde um deles expirar ou cancele um aluguel.");
        }

        devolverLicencasVencidas(filme, agora);
        if (!filme.temLicencaDisponivel()) {
            throw new RegraNegocioException("Não há licenças disponíveis para este filme no momento.");
        }

        ResultadoPagamento pagamento = pagamentoGateway.cobrar(regras.valor(), "Aluguel do filme " + filme.getTitulo());
        if (!pagamento.aprovado()) {
            throw new RegraNegocioException("Pagamento recusado (simulação). Nenhum valor foi cobrado.");
        }

        filme.retirarLicenca();
        usuario.registrarAluguel(agora);
        Aluguel aluguel = Aluguel.novo(usuario, filme, agora, regras.duracao(), regras.valor(),
                pagamento.transacao(), UUID.randomUUID().toString());
        // saveAndFlush: um conflito de versão (aluguel simultâneo) aparece aqui e vira HTTP 409.
        return AluguelResponse.de(aluguelRepository.saveAndFlush(aluguel), agora, regras.prazoCancelamento());
    }

    @Transactional
    public AluguelResponse cancelar(Long usuarioId, Long aluguelId) {
        Instant agora = agora();
        Aluguel aluguel = aluguelRepository.findById(aluguelId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluguel não encontrado."));
        if (!aluguel.pertenceA(usuarioId)) {
            throw new AcessoNegadoException("Você só pode cancelar os seus próprios aluguéis.");
        }

        StatusAluguel status = aluguel.statusEm(agora);
        if (status == StatusAluguel.CANCELADO) {
            throw new RegraNegocioException("Este aluguel já foi cancelado.");
        }
        if (status == StatusAluguel.EXPIRADO) {
            throw new RegraNegocioException("Este aluguel já expirou e não pode mais ser cancelado.");
        }
        if (!aluguel.podeSerCanceladoEm(agora, regras.prazoCancelamento())) {
            throw new RegraNegocioException("O prazo para cancelar (" + descrever(regras.prazoCancelamento())
                    + " após o aluguel) já terminou.");
        }

        String reembolso = pagamentoGateway.reembolsar(aluguel.getTransacaoPagamento(), aluguel.getValor());
        aluguel.cancelar(agora, reembolso);
        aluguel.getFilme().devolverLicenca();
        aluguelRepository.flush();
        return AluguelResponse.de(aluguel, agora, regras.prazoCancelamento());
    }

    /** Histórico do cliente, do mais recente para o mais antigo. */
    @Transactional(readOnly = true)
    public List<AluguelResponse> listarDoUsuario(Long usuarioId) {
        Instant agora = agora();
        return aluguelRepository.findByUsuarioIdOrderByDataAluguelDesc(usuarioId).stream()
                .map(aluguel -> AluguelResponse.de(aluguel, agora, regras.prazoCancelamento()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AluguelResponse> listarTodos() {
        Instant agora = agora();
        return aluguelRepository.findAllByOrderByDataAluguelDesc().stream()
                .map(aluguel -> AluguelResponse.de(aluguel, agora, regras.prazoCancelamento()))
                .toList();
    }

    /**
     * Valida um código de acesso (adaptação da validação de entrada do ingresso).
     * Não consome o código: o filme pode ser assistido várias vezes enquanto o aluguel vale.
     */
    @Transactional(readOnly = true)
    public ValidacaoAcessoResponse validarAcesso(String codigo) {
        Instant agora = agora();
        Aluguel aluguel = aluguelRepository.findByCodigoAcesso(codigo.trim())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Código de acesso não encontrado."));
        StatusAluguel status = aluguel.statusEm(agora);
        String mensagem = switch (status) {
            case ATIVO -> "Acesso liberado: o aluguel está ativo.";
            case EXPIRADO -> "Acesso negado: o aluguel expirou.";
            case CANCELADO -> "Acesso negado: o aluguel foi cancelado.";
        };
        return new ValidacaoAcessoResponse(status == StatusAluguel.ATIVO, mensagem, status,
                aluguel.getFilme().getTitulo(), aluguel.getUsuario().getNome(), aluguel.getDataExpiracao());
    }

    /**
     * Marca como expirados os aluguéis vencidos e devolve as licenças.
     * Cada aluguel usa a sua própria transação: um conflito pontual não desfaz os demais
     * e é tentado de novo na próxima execução.
     */
    public int expirarVencidos() {
        Instant agora = agora();
        int expirados = 0;
        for (Long id : aluguelRepository.buscarIdsVencidos(StatusAluguel.ATIVO, agora)) {
            try {
                if (Boolean.TRUE.equals(transacao.execute(status -> expirarSeVencido(id, agora)))) {
                    expirados++;
                }
            } catch (RuntimeException e) {
                log.warn("Não foi possível expirar o aluguel {} agora; nova tentativa na próxima execução: {}",
                        id, e.getMessage());
            }
        }
        return expirados;
    }

    private boolean expirarSeVencido(Long id, Instant agora) {
        Aluguel aluguel = aluguelRepository.findById(id).orElse(null);
        if (aluguel == null || !aluguel.estaVencido(agora)) {
            return false;
        }
        aluguel.expirar();
        aluguel.getFilme().devolverLicenca();
        aluguelRepository.flush();
        return true;
    }

    /** Antes de checar a disponibilidade, devolve as licenças de aluguéis do filme que já venceram. */
    private void devolverLicencasVencidas(Filme filme, Instant agora) {
        List<Aluguel> vencidos = aluguelRepository.findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(
                filme.getId(), StatusAluguel.ATIVO, agora);
        for (Aluguel vencido : vencidos) {
            vencido.expirar();
            filme.devolverLicenca();
        }
    }

    private Instant agora() {
        return Instant.now(clock);
    }

    static String descrever(Duration duracao) {
        long minutos = duracao.toMinutes();
        if (minutos % 60 == 0) {
            long horas = minutos / 60;
            return horas == 1 ? "1 hora" : horas + " horas";
        }
        return minutos == 1 ? "1 minuto" : minutos + " minutos";
    }
}
