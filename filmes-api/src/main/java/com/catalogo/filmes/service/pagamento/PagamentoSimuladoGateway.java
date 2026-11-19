package com.catalogo.filmes.service.pagamento;

import com.catalogo.filmes.config.PagamentoSimuladoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoSimuladoGateway implements GatewayDeCobranca, GatewayDeReembolso {

    private final PagamentoSimuladoProperties propriedades;

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
