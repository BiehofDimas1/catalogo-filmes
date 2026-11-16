package com.catalogo.filmes.service.pagamento;

import com.catalogo.filmes.config.PagamentoSimuladoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pagamento e reembolso simulados: nenhum dinheiro é movimentado.
 * O resultado da cobrança vem só da configuração do servidor ({@code pagamento.simulado.resultado}).
 */
@Component
public class PagamentoSimuladoGateway implements PagamentoGateway {

    private static final Logger log = LoggerFactory.getLogger(PagamentoSimuladoGateway.class);

    private final PagamentoSimuladoProperties propriedades;

    public PagamentoSimuladoGateway(PagamentoSimuladoProperties propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public ResultadoPagamento cobrar(BigDecimal valor, String descricao) {
        if (propriedades.resultado() == PagamentoSimuladoProperties.Resultado.RECUSADO) {
            log.info("Pagamento simulado RECUSADO: R$ {} ({})", valor, descricao);
            return ResultadoPagamento.recusado();
        }
        String transacao = "PAG-SIM-" + UUID.randomUUID();
        log.info("Pagamento simulado APROVADO: R$ {} ({}) -> {}", valor, descricao, transacao);
        return ResultadoPagamento.aprovado(transacao);
    }

    @Override
    public String reembolsar(String transacaoPagamento, BigDecimal valor) {
        String reembolso = "REE-SIM-" + UUID.randomUUID();
        log.info("Reembolso simulado de R$ {} para {} -> {}", valor, transacaoPagamento, reembolso);
        return reembolso;
    }
}
