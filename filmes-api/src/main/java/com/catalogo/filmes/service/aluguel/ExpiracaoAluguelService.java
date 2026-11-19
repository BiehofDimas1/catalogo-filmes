package com.catalogo.filmes.service.aluguel;

import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.model.StatusAluguel;
import com.catalogo.filmes.repository.AluguelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ExpiracaoAluguelService {

    private final AluguelRepository aluguelRepository;
    private final Clock clock;
    private final TransactionTemplate transacao;

    public ExpiracaoAluguelService(AluguelRepository aluguelRepository, Clock clock,
                                   PlatformTransactionManager transactionManager) {
        this.aluguelRepository = aluguelRepository;
        this.clock = clock;
        this.transacao = new TransactionTemplate(transactionManager);
    }

    public int expirarVencidos() {
        Instant agora = Instant.now(clock);
        int expirados = 0;
        for (Long id : aluguelRepository.buscarIdsVencidos(StatusAluguel.ATIVO, agora)) {
            if (tentarExpirar(id, agora)) {
                expirados++;
            }
        }
        return expirados;
    }

    public void liberarLicencasVencidas(Filme filme, Instant agora) {
        List<Aluguel> vencidos = aluguelRepository.findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(
                filme.getId(), StatusAluguel.ATIVO, agora);
        for (Aluguel vencido : vencidos) {
            vencido.expirar();
            filme.devolverLicenca();
        }
    }

    private boolean tentarExpirar(Long id, Instant agora) {
        try {
            return Boolean.TRUE.equals(transacao.execute(status -> expirarSeVencido(id, agora)));
        } catch (RuntimeException e) {
            log.warn("Não foi possível expirar o aluguel {} agora; nova tentativa na próxima execução: {}",
                    id, e.getMessage());
            return false;
        }
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
}
