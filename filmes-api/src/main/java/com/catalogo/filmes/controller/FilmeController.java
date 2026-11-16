package com.catalogo.filmes.controller;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.security.UsuarioLogado;
import com.catalogo.filmes.service.FilmeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Catálogo de filmes. Consultas para qualquer usuário logado; alterações só para ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/filmes")
@RequiredArgsConstructor
public class FilmeController {

    private final FilmeService service;

    @GetMapping
    public List<FilmeResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario) {
        return service.listar(usuario.isAdmin());
    }

    @GetMapping("/{id}")
    public FilmeResponse buscar(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioLogado usuario) {
        return service.buscar(id, usuario.isAdmin());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmeResponse cadastrar(@Valid @RequestBody FilmeRequest dados) {
        return service.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public FilmeResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody FilmeRequest dados) {
        return service.atualizar(id, dados);
    }

    /** Inativa o filme (não há exclusão física). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable("id") Long id) {
        service.inativar(id);
    }

    @PatchMapping("/{id}/reativar")
    public FilmeResponse reativar(@PathVariable("id") Long id) {
        return service.reativar(id);
    }
}
