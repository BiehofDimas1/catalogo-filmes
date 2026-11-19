package com.catalogo.filmes.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.catalogo.filmes.config.JwtProperties;
import com.catalogo.filmes.model.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
public class JwtTokenService implements GeradorDeToken, VerificadorDeToken {

    private static final String EMISSOR = "filmes-api";
    private static final int TAMANHO_MINIMO_SEGREDO = 32;
    private static final int TAMANHO_CHAVE_ALEATORIA = 64;

    private final Algorithm algoritmo;
    private final JWTVerifier verificador;
    private final Duration validade;

    public JwtTokenService(JwtProperties propriedades) {
        this.algoritmo = Algorithm.HMAC256(chave(propriedades.secret()));
        this.verificador = JWT.require(algoritmo).withIssuer(EMISSOR).build();
        this.validade = propriedades.validade();
    }

    @Override
    public TokenGerado gerar(Usuario usuario) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiraEm = agora.plus(validade);
        String token = JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(usuario.getEmail())
                .withIssuedAt(agora)
                .withExpiresAt(expiraEm)
                .sign(algoritmo);
        return new TokenGerado(token, expiraEm);
    }

    @Override
    public Optional<String> verificar(String token) {
        try {
            return Optional.ofNullable(verificador.verify(token).getSubject());
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    private static byte[] chave(String segredo) {
        if (segredo == null || segredo.isBlank()) {
            log.warn("JWT_SECRET não definido: usando uma chave aleatória. "
                    + "Os tokens deixam de valer quando a API reinicia.");
            return chaveAleatoria();
        }
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " bytes.");
        }
        return bytes;
    }

    private static byte[] chaveAleatoria() {
        byte[] chave = new byte[TAMANHO_CHAVE_ALEATORIA];
        new SecureRandom().nextBytes(chave);
        return chave;
    }
}
