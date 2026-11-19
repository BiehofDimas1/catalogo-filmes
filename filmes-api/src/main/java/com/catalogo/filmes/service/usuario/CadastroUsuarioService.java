package com.catalogo.filmes.service.usuario;

import com.catalogo.filmes.dto.CadastroRequest;
import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CadastroUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest dados) {
        String email = Usuario.normalizarEmail(dados.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("Já existe uma conta com este e-mail.");
        }
        Usuario usuario = new Usuario(dados.nome().trim(), email, passwordEncoder.encode(dados.senha()),
                Perfil.CLIENTE, Instant.now(clock));
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }
}
