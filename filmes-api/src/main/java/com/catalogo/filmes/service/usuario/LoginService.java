package com.catalogo.filmes.service.usuario;

import com.catalogo.filmes.dto.LoginRequest;
import com.catalogo.filmes.dto.LoginResponse;
import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.security.GeradorDeToken;
import com.catalogo.filmes.security.TokenGerado;
import com.catalogo.filmes.service.exception.CredenciaisInvalidasException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeradorDeToken geradorDeToken;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmail(Usuario.normalizarEmail(dados.email()))
                .filter(encontrado -> passwordEncoder.matches(dados.senha(), encontrado.getSenha()))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos."));
        TokenGerado token = geradorDeToken.gerar(usuario);
        return new LoginResponse(token.token(), "Bearer", token.expiraEm(), UsuarioResponse.de(usuario));
    }
}
