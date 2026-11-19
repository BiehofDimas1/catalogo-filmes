package com.catalogo.filmes.service.usuario;

import com.catalogo.filmes.config.JwtProperties;
import com.catalogo.filmes.dto.LoginRequest;
import com.catalogo.filmes.dto.LoginResponse;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.security.JwtTokenService;
import com.catalogo.filmes.service.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes!!";

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtTokenService tokens =
            new JwtTokenService(new JwtProperties(SEGREDO, Duration.ofHours(2)));
    private final LoginService service = new LoginService(repository, encoder, tokens);

    private void anaCadastradaComSenha(String senha) {
        String hash = encoder.encode(senha);
        Usuario ana = new Usuario("Ana", "ana@teste.com", hash, Perfil.CLIENTE, Instant.now());
        when(repository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
    }

    @Test
    void loginValidoDevolveTokenDoUsuario() {
        anaCadastradaComSenha("senha123");

        LoginResponse resposta = service.login(new LoginRequest("Ana@Teste.com", "senha123"));

        assertEquals("Bearer", resposta.tipo());
        assertEquals(Optional.of("ana@teste.com"), tokens.verificar(resposta.token()));
        assertEquals(Perfil.CLIENTE, resposta.usuario().perfil());
    }

    @Test
    void loginComSenhaErradaDeveFalhar() {
        anaCadastradaComSenha("senha123");

        assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequest("ana@teste.com", "errada1")));
    }

    @Test
    void loginComEmailInexistenteDeveFalharComAMesmaMensagem() {
        when(repository.findByEmail("ninguem@teste.com")).thenReturn(Optional.empty());

        CredenciaisInvalidasException erro = assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequest("ninguem@teste.com", "senha123")));
        assertEquals("E-mail ou senha inválidos.", erro.getMessage());
    }
}
