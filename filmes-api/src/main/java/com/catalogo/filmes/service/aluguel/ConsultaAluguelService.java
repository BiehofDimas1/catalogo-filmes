package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.dto.RegrasAluguelResponse;
import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.repository.AluguelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaAluguelService {

    private final AluguelRepository aluguelRepository;
    private final AluguelProperties regras;
    private final Clock clock;

    public RegrasAluguelResponse regras() {
        return new RegrasAluguelResponse(regras.valor(), regras.duracao().toHours(),
                regras.prazoCancelamento().toMinutes(), regras.limiteAtivos());
    }

    @Transactional(readOnly = true)
    public List<AluguelResponse> listarDoUsuario(Long usuarioId) {
        return paraResposta(aluguelRepository.findByUsuarioIdOrderByDataAluguelDesc(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<AluguelResponse> listarTodos() {
        return paraResposta(aluguelRepository.findAllByOrderByDataAluguelDesc());
    }

    private List<AluguelResponse> paraResposta(List<Aluguel> alugueis) {
        Instant agora = Instant.now(clock);
        return alugueis.stream()
                .map(aluguel -> AluguelResponse.de(aluguel, agora, regras.prazoCancelamento()))
                .toList();
    }
}
