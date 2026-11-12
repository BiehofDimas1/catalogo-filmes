package com.catalogo.filmes.service;

import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmeService {

    private final FilmeRepository repository;

    public List<Filme> listar() {
        return repository.findAll();
    }

    public Filme buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filme não encontrado"));
    }

    public Filme cadastrar(Filme filme) {
        filme.setId(null);
        filme.setAtivo(true);
        return repository.save(filme);
    }

    public Filme atualizar(Long id, Filme dados) {
        Filme filme = buscarPorId(id);
        filme.setTitulo(dados.getTitulo());
        filme.setDuracaoMinutos(dados.getDuracaoMinutos());
        filme.setLicencasDisponiveis(dados.getLicencasDisponiveis());
        return repository.save(filme);
    }

    public void inativar(Long id) {
        Filme filme = buscarPorId(id);
        filme.setAtivo(false);
        repository.save(filme);
    }
}