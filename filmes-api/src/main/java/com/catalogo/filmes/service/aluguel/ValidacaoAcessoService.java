package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.dto.ValidacaoAcessoResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.repository.AluguelRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ValidacaoAcessoService {

    private final AluguelRepository aluguelRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ValidacaoAcessoResponse validar(String codigo) {
        Instant agora = Instant.now(clock);
        Aluguel aluguel = aluguelRepository.findByCodigoAcesso(codigo.trim())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Código de acesso não encontrado."));
        StatusAluguel status = aluguel.statusEm(agora);
        return new ValidacaoAcessoResponse(status == StatusAluguel.ATIVO, mensagem(status), status,
                aluguel.getFilme().getTitulo(), aluguel.getUsuario().getNome(), aluguel.getDataExpiracao());
    }

    private static String mensagem(StatusAluguel status) {
        return switch (status) {
            case ATIVO -> "Acesso liberado: o aluguel está ativo.";
            case EXPIRADO -> "Acesso negado: o aluguel expirou.";
            case CANCELADO -> "Acesso negado: o aluguel foi cancelado.";
        };
    }
}
