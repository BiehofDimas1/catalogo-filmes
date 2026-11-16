package com.catalogo.filmes.service;

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
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    /** Cadastro público: sempre cria CLIENTE, com a senha gravada em BCrypt. */
    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest dados) {
        String email = normalizarEmail(dados.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("Já existe uma conta com este e-mail.");
        }
        Usuario usuario = new Usuario(dados.nome().trim(), email, passwordEncoder.encode(dados.senha()),
                Perfil.CLIENTE, Instant.now(clock));
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    /** A mesma mensagem para e-mail inexistente e senha errada, para não revelar quais contas existem. */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(dados.email()))
                .filter(encontrado -> passwordEncoder.matches(dados.senha(), encontrado.getSenha()))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos."));
        TokenService.TokenGerado token = tokenService.gerar(usuario);
        return new LoginResponse(token.token(), "Bearer", token.expiraEm(), UsuarioResponse.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
