package com.catalogo.filmes.controller;

import com.catalogo.filmes.dto.AluguelRequest;
import com.catalogo.filmes.dto.AluguelResponse;
import com.catalogo.filmes.dto.RegrasAluguelResponse;
import com.catalogo.filmes.dto.ValidacaoAcessoResponse;
import com.catalogo.filmes.security.UsuarioLogado;
import com.catalogo.filmes.service.AluguelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Fluxo principal: aluguel de filmes. As permissões por perfil estão em SecurityConfig. */
@RestController
@RequestMapping("/alugueis")
@RequiredArgsConstructor
public class AluguelController {

    private final AluguelService service;

    /** Preço, validade e limites atuais (qualquer usuário logado). */
    @GetMapping("/regras")
    public RegrasAluguelResponse regras() {
        return service.regras();
    }

    /** CLIENTE aluga um filme. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AluguelResponse alugar(@Valid @RequestBody AluguelRequest dados,
                                  @AuthenticationPrincipal UsuarioLogado usuario) {
        return service.alugar(usuario.id(), dados.filmeId());
    }

    /** CLIENTE consulta o próprio histórico. */
    @GetMapping("/meus")
    public List<AluguelResponse> meus(@AuthenticationPrincipal UsuarioLogado usuario) {
        return service.listarDoUsuario(usuario.id());
    }

    /** CLIENTE cancela um aluguel próprio dentro do prazo. */
    @PostMapping("/{id}/cancelar")
    public AluguelResponse cancelar(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioLogado usuario) {
        return service.cancelar(usuario.id(), id);
    }

    /** ADMIN consulta todos os aluguéis. */
    @GetMapping
    public List<AluguelResponse> todos() {
        return service.listarTodos();
    }

    /** ADMIN valida um código de acesso (não consome o código). */
    @GetMapping("/acesso/{codigo}")
    public ValidacaoAcessoResponse validarAcesso(@PathVariable("codigo") String codigo) {
        return service.validarAcesso(codigo);
    }
}
