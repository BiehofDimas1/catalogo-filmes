package com.catalogo.filmes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "alugueis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;

    @Column(name = "data_aluguel", nullable = false)
    private Instant dataAluguel;

    @Column(name = "data_expiracao", nullable = false)
    private Instant dataExpiracao;

    @Column(name = "data_cancelamento")
    private Instant dataCancelamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAluguel status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "transacao_pagamento", nullable = false, length = 60)
    private String transacaoPagamento;

    @Column(name = "transacao_reembolso", length = 60)
    private String transacaoReembolso;

    @Column(name = "codigo_acesso", nullable = false, unique = true, length = 36)
    private String codigoAcesso;

    @Version
    private Long versao;

    public static Aluguel novo(Usuario usuario, Filme filme, Instant agora, Duration duracao,
                               BigDecimal valor, String transacaoPagamento, String codigoAcesso) {
        Aluguel aluguel = new Aluguel();
        aluguel.usuario = usuario;
        aluguel.filme = filme;
        aluguel.dataAluguel = agora;
        aluguel.dataExpiracao = agora.plus(duracao);
        aluguel.status = StatusAluguel.ATIVO;
        aluguel.valor = valor;
        aluguel.transacaoPagamento = transacaoPagamento;
        aluguel.codigoAcesso = codigoAcesso;
        return aluguel;
    }

    public StatusAluguel statusEm(Instant instante) {
        return estaVencido(instante) ? StatusAluguel.EXPIRADO : status;
    }

    public boolean estaVencido(Instant instante) {
        return status == StatusAluguel.ATIVO && !instante.isBefore(dataExpiracao);
    }

    public Instant cancelavelAte(Duration prazo) {
        return dataAluguel.plus(prazo);
    }

    public boolean podeSerCanceladoEm(Instant instante, Duration prazo) {
        return statusEm(instante) == StatusAluguel.ATIVO && !instante.isAfter(cancelavelAte(prazo));
    }

    public boolean pertenceA(Long usuarioId) {
        return usuario.getId() != null && usuario.getId().equals(usuarioId);
    }

    public void cancelar(Instant instante, String transacaoReembolso) {
        this.status = StatusAluguel.CANCELADO;
        this.dataCancelamento = instante;
        this.transacaoReembolso = transacaoReembolso;
    }

    public void expirar() {
        this.status = StatusAluguel.EXPIRADO;
    }
}
