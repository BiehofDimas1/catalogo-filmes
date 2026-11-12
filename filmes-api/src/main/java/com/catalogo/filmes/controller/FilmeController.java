package com.catalogo.filmes.controller;

import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.service.FilmeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/filmes")
@RequiredArgsConstructor
public class FilmeController {

    private final FilmeService service;

    @GetMapping
    public List<Filme> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Filme buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Filme cadastrar(@Valid @RequestBody Filme filme) {
        return service.cadastrar(filme);
    }

    @PutMapping("/{id}")
    public Filme atualizar(@PathVariable Long id, @Valid @RequestBody Filme filme) {
        return service.atualizar(id, filme);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }
}