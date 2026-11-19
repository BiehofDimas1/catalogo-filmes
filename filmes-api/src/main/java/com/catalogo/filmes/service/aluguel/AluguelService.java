package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.service.aluguel.validacao.PedidoAluguel;
import com.catalogo.filmes.service.aluguel.validacao.ValidadorAluguel;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import com.catalogo.filmes.service.pagamento.GatewayDeCobranca;
import com.catalogo.filmes.service.pagamento.ResultadoPagamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExpiracaoAluguelService expiracao;
    private final List<ValidadorAluguel> validadores;
    private final GatewayDeCobranca cobranca;
    private final AluguelProperties regras;
    private final Clock clock;

    @Transactional
    public AluguelResponse alugar(Long usuarioId, Long filmeId) {
        Instant agora = Instant.now(clock);
        Usuario usuario = buscarUsuario(usuarioId);
        Filme filme = buscarFilme(filmeId);

        expiracao.liberarLicencasVencidas(filme, agora);
        validar(new PedidoAluguel(usuarioId, filme, agora));
        String transacao = cobrar(filme);

        Aluguel aluguel = registrar(usuario, filme, transacao, agora);
        return AluguelResponse.de(aluguel, agora, regras.prazoCancelamento());
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private Filme buscarFilme(Long id) {
        return filmeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));
    }

    private void validar(PedidoAluguel pedido) {
        validadores.forEach(validador -> validador.validar(pedido));
    }

    private String cobrar(Filme filme) {
        String descricao = "Aluguel do filme " + filme.getTitulo();
        ResultadoPagamento pagamento = cobranca.cobrar(regras.valor(), descricao);
        if (!pagamento.aprovado()) {
            throw new RegraNegocioException("Pagamento recusado (simulação). Nenhum valor foi cobrado.");
        }
        return pagamento.transacao();
    }

    private Aluguel registrar(Usuario usuario, Filme filme, String transacao, Instant agora) {
        filme.retirarLicenca();
        usuario.registrarAluguel(agora);
        String codigoAcesso = UUID.randomUUID().toString();
        Aluguel aluguel = Aluguel.novo(usuario, filme, agora, regras.duracao(), regras.valor(),
                transacao, codigoAcesso);
        return aluguelRepository.saveAndFlush(aluguel);
    }
}
