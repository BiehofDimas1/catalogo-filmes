package com.catalogo.filmes.security;

import com.catalogo.filmes.config.JwtProperties;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes!!";

    private final TokenService service = new TokenService(new JwtProperties(SEGREDO, Duration.ofHours(2)));

    private Usuario usuario() {
        return new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, Instant.now());
    }

    @Test
    void tokenGeradoIdentificaOUsuario() {
        TokenService.TokenGerado gerado = service.gerar(usuario());

        assertEquals(Optional.of("ana@teste.com"), service.validar(gerado.token()));
        assertTrue(gerado.expiraEm().isAfter(Instant.now().plus(Duration.ofMinutes(119))));
    }

    @Test
    void tokenAlteradoEhRecusado() {
        String token = service.gerar(usuario()).token();
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertTrue(service.validar(alterado).isEmpty());
        assertTrue(service.validar("nao-e-um-jwt").isEmpty());
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        TokenService outro = new TokenService(new JwtProperties("outro-segredo-diferente-com-mais-de-32-bytes", Duration.ofHours(2)));

        assertTrue(service.validar(outro.gerar(usuario()).token()).isEmpty());
    }

    @Test
    void tokenExpiradoEhRecusado() {
        TokenService expirado = new TokenService(new JwtProperties(SEGREDO, Duration.ofSeconds(-30)));

        assertTrue(service.validar(expirado.gerar(usuario()).token()).isEmpty());
    }

    @Test
    void segredoCurtoImpedeOInicio() {
        assertThrows(IllegalStateException.class,
                () -> new TokenService(new JwtProperties("curto", Duration.ofHours(2))));
    }

    @Test
    void semSegredoUsaChaveAleatoria() {
        TokenService aleatorio = new TokenService(new JwtProperties("", Duration.ofHours(2)));

        String token = aleatorio.gerar(usuario()).token();

        assertEquals(Optional.of("ana@teste.com"), aleatorio.validar(token));
        assertTrue(service.validar(token).isEmpty());
    }
}
