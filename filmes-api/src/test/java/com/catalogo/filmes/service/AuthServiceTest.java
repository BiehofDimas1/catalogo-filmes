package com.catalogo.filmes.service;

import com.catalogo.filmes.config.JwtProperties;
import com.catalogo.filmes.dto.CadastroRequest;
import com.catalogo.filmes.dto.LoginRequest;
import com.catalogo.filmes.dto.LoginResponse;
import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.security.TokenService;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final TokenService tokenService =
            new TokenService(new JwtProperties("segredo-de-teste-com-mais-de-32-bytes!!", Duration.ofHours(2)));
    private final AuthService service = new AuthService(repository, encoder, tokenService, Clock.systemUTC());

    private Usuario usuarioSalvo(String email, String senha) {
        return new Usuario("Ana", email, encoder.encode(senha), Perfil.CLIENTE, Instant.now());
    }

    @Test
    void cadastroSempreCriaClienteComSenhaCriptografada() {
        when(repository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse resposta = service.cadastrar(new CadastroRequest(" Ana ", " Ana@Teste.com ", "senha123"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(salvo.capture());
        assertEquals(Perfil.CLIENTE, salvo.getValue().getPerfil());
        assertEquals("ana@teste.com", salvo.getValue().getEmail());
        assertEquals("Ana", salvo.getValue().getNome());
        assertNotEquals("senha123", salvo.getValue().getSenha());
        assertTrue(encoder.matches("senha123", salvo.getValue().getSenha()));
        assertEquals(Perfil.CLIENTE, resposta.perfil());
    }

    @Test
    void cadastroComEmailJaUsadoDeveFalhar() {
        when(repository.existsByEmail("ana@teste.com")).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.cadastrar(new CadastroRequest("Ana", "ANA@teste.com", "senha123")));
        verify(repository, never()).save(any());
    }

    @Test
    void loginValidoDevolveTokenDoUsuario() {
        when(repository.findByEmail("ana@teste.com")).thenReturn(Optional.of(usuarioSalvo("ana@teste.com", "senha123")));

        LoginResponse resposta = service.login(new LoginRequest("Ana@Teste.com", "senha123"));

        assertEquals("Bearer", resposta.tipo());
        assertEquals(Optional.of("ana@teste.com"), tokenService.validar(resposta.token()));
        assertEquals(Perfil.CLIENTE, resposta.usuario().perfil());
    }

    @Test
    void loginComSenhaErradaDeveFalhar() {
        when(repository.findByEmail("ana@teste.com")).thenReturn(Optional.of(usuarioSalvo("ana@teste.com", "senha123")));

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
