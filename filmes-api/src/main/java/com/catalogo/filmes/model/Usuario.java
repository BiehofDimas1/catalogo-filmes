package com.catalogo.filmes.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Sempre em minúsculas; é o login do usuário. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Hash BCrypt da senha; a senha em texto nunca é gravada. */
    @Column(nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    /**
     * Atualizado a cada aluguel. Junto com a versão, faz dois aluguéis simultâneos
     * do mesmo usuário entrarem em conflito, para que o limite de aluguéis ativos não seja furado.
     */
    @Column(name = "ultimo_aluguel_em")
    private Instant ultimoAluguelEm;

    @Version
    private Long versao;

    public Usuario(String nome, String email, String senhaCriptografada, Perfil perfil, Instant criadoEm) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaCriptografada;
        this.perfil = perfil;
        this.criadoEm = criadoEm;
    }

    public void registrarAluguel(Instant instante) {
        this.ultimoAluguelEm = instante;
    }
}
