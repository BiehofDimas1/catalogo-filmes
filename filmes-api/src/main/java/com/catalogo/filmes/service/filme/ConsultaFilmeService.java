package com.catalogo.filmes.service.filme;

import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaFilmeService {

    private final FilmeRepository repository;

    @Transactional(readOnly = true)
    public List<FilmeResponse> listar(boolean incluirInativos) {
        List<Filme> filmes = incluirInativos
                ? repository.findAllByOrderByTituloNormalizadoAsc()
                : repository.findByAtivoTrueOrderByTituloNormalizadoAsc();
        return filmes.stream().map(FilmeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FilmeResponse buscar(Long id, boolean incluirInativos) {
        return repository.findById(id)
                .filter(filme -> filme.isAtivo() || incluirInativos)
                .map(FilmeResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));
    }
}
