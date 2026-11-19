package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.service.aluguel.validacao.PedidoCancelamento;
import com.catalogo.filmes.service.aluguel.validacao.ValidadorCancelamento;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.pagamento.GatewayDeReembolso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelamentoAluguelService {

    private final AluguelRepository aluguelRepository;
    private final List<ValidadorCancelamento> validadores;
    private final GatewayDeReembolso reembolso;
    private final AluguelProperties regras;
    private final Clock clock;

    @Transactional
    public AluguelResponse cancelar(Long usuarioId, Long aluguelId) {
        Instant agora = Instant.now(clock);
        Aluguel aluguel = buscarAluguel(aluguelId);

        validar(new PedidoCancelamento(usuarioId, aluguel, agora));
        estornar(aluguel, agora);

        return AluguelResponse.de(aluguel, agora, regras.prazoCancelamento());
    }

    private Aluguel buscarAluguel(Long id) {
        return aluguelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluguel não encontrado."));
    }

    private void validar(PedidoCancelamento pedido) {
        validadores.forEach(validador -> validador.validar(pedido));
    }

    private void estornar(Aluguel aluguel, Instant agora) {
        String transacaoReembolso = reembolso.reembolsar(aluguel.getTransacaoPagamento(), aluguel.getValor());
        aluguel.cancelar(agora, transacaoReembolso);
        aluguel.getFilme().devolverLicenca();
        aluguelRepository.flush();
    }
}
