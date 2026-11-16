package com.catalogo.filmes.controller;

import com.catalogo.filmes.dto.CadastroRequest;
import com.catalogo.filmes.dto.LoginRequest;
import com.catalogo.filmes.dto.LoginResponse;
import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.security.UsuarioLogado;
import com.catalogo.filmes.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    /** Público. Cria sempre uma conta CLIENTE. */
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return service.cadastrar(dados);
    }

    /** Público. Devolve o JWT para as próximas chamadas. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.login(dados);
    }

    /** Dados do usuário logado, com o perfil atual do banco. */
    @GetMapping("/me")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioLogado usuario) {
        return service.buscar(usuario.id());
    }
}
