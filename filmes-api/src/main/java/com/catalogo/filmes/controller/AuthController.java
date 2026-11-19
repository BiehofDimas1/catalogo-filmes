package com.catalogo.filmes.controller;

import com.catalogo.filmes.dto.CadastroRequest;
import com.catalogo.filmes.dto.LoginRequest;
import com.catalogo.filmes.dto.LoginResponse;
import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.security.UsuarioLogado;
import com.catalogo.filmes.service.usuario.CadastroUsuarioService;
import com.catalogo.filmes.service.usuario.ConsultaUsuarioService;
import com.catalogo.filmes.service.usuario.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CadastroUsuarioService cadastroService;
    private final LoginService loginService;
    private final ConsultaUsuarioService consultaService;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return cadastroService.cadastrar(dados);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return loginService.login(dados);
    }

    @GetMapping("/me")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioLogado usuario) {
        return consultaService.buscar(usuario.id());
    }
}
