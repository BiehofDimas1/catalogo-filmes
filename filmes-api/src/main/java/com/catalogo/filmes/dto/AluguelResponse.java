package com.catalogo.filmes.dto;

import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.StatusAluguel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Aluguel como a interface o vê. O status é o efetivo no momento da consulta:
 * um aluguel ativo que já venceu aparece como EXPIRADO.
 */
public record AluguelResponse(
        Long id,
        Long filmeId,
        String filmeTitulo,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        StatusAluguel status,
        Instant dataAluguel,
        Instant dataExpiracao,
        Instant dataCancelamento,
        Instant cancelavelAte,
        boolean cancelavel,
        BigDecimal valor,
        String transacaoPagamento,
        String transacaoReembolso,
        String codigoAcesso) {

    public static AluguelResponse de(Aluguel aluguel, Instant agora, Duration prazoCancelamento) {
        return new AluguelResponse(
                aluguel.getId(),
                aluguel.getFilme().getId(),
                aluguel.getFilme().getTitulo(),
                aluguel.getUsuario().getId(),
                aluguel.getUsuario().getNome(),
                aluguel.getUsuario().getEmail(),
                aluguel.statusEm(agora),
                aluguel.getDataAluguel(),
                aluguel.getDataExpiracao(),
                aluguel.getDataCancelamento(),
                aluguel.cancelavelAte(prazoCancelamento),
                aluguel.podeSerCanceladoEm(agora, prazoCancelamento),
                aluguel.getValor(),
                aluguel.getTransacaoPagamento(),
                aluguel.getTransacaoReembolso(),
                aluguel.getCodigoAcesso());
    }
}
