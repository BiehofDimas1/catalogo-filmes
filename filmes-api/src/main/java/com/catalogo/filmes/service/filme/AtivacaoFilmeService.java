package com.catalogo.filmes.service.filme;

import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtivacaoFilmeService {

    private final FilmeRepository repository;

    @Transactional
    public void inativar(Long id) {
        buscar(id).inativar();
    }

    @Transactional
    public FilmeResponse reativar(Long id) {
        Filme filme = buscar(id);
        filme.reativar();
        return FilmeResponse.de(repository.saveAndFlush(filme));
    }

    private Filme buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));
    }
}
