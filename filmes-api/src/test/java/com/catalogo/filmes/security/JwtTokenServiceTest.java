package com.catalogo.filmes.security;

import com.catalogo.filmes.config.JwtProperties;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes!!";

    private final JwtTokenService service =
            new JwtTokenService(new JwtProperties(SEGREDO, Duration.ofHours(2)));

    private Usuario usuario() {
        return new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, Instant.now());
    }

    @Test
    void tokenGeradoIdentificaOUsuario() {
        TokenGerado gerado = service.gerar(usuario());

        assertEquals(Optional.of("ana@teste.com"), service.verificar(gerado.token()));
        assertTrue(gerado.expiraEm().isAfter(Instant.now().plus(Duration.ofMinutes(119))));
    }

    @Test
    void tokenAlteradoEhRecusado() {
        String token = service.gerar(usuario()).token();
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertTrue(service.verificar(alterado).isEmpty());
        assertTrue(service.verificar("nao-e-um-jwt").isEmpty());
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        JwtTokenService outro = new JwtTokenService(
                new JwtProperties("outro-segredo-diferente-com-mais-de-32-bytes", Duration.ofHours(2)));

        assertTrue(service.verificar(outro.gerar(usuario()).token()).isEmpty());
    }

    @Test
    void tokenExpiradoEhRecusado() {
        JwtTokenService expirado = new JwtTokenService(new JwtProperties(SEGREDO, Duration.ofSeconds(-30)));

        assertTrue(service.verificar(expirado.gerar(usuario()).token()).isEmpty());
    }

    @Test
    void segredoCurtoImpedeOInicio() {
        assertThrows(IllegalStateException.class,
                () -> new JwtTokenService(new JwtProperties("curto", Duration.ofHours(2))));
    }

    @Test
    void semSegredoUsaChaveAleatoria() {
        JwtTokenService aleatorio = new JwtTokenService(new JwtProperties("", Duration.ofHours(2)));

        String token = aleatorio.gerar(usuario()).token();

        assertEquals(Optional.of("ana@teste.com"), aleatorio.verificar(token));
        assertTrue(service.verificar(token).isEmpty());
    }
}
