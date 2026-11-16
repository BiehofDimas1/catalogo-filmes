package com.catalogo.filmes.service;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmeService {

    private final FilmeRepository repository;

    /** O ADMIN vê todos os filmes; o CLIENTE só vê os ativos. */
    @Transactional(readOnly = true)
    public List<FilmeResponse> listar(boolean incluirInativos) {
        List<Filme> filmes = incluirInativos
                ? repository.findAllByOrderByTituloNormalizadoAsc()
                : repository.findByAtivoTrueOrderByTituloNormalizadoAsc();
        return filmes.stream().map(FilmeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FilmeResponse buscar(Long id, boolean incluirInativos) {
        Filme filme = buscarEntidade(id);
        if (!filme.isAtivo() && !incluirInativos) {
            throw new RecursoNaoEncontradoException("Filme não encontrado.");
        }
        return FilmeResponse.de(filme);
    }

    @Transactional
    public FilmeResponse cadastrar(FilmeRequest dados) {
        garantirTituloUnico(dados.titulo(), null);
        Filme filme = new Filme();
        aplicar(filme, dados);
        return FilmeResponse.de(repository.save(filme));
    }

    @Transactional
    public FilmeResponse atualizar(Long id, FilmeRequest dados) {
        Filme filme = buscarEntidade(id);
        garantirTituloUnico(dados.titulo(), id);
        aplicar(filme, dados);
        return FilmeResponse.de(repository.saveAndFlush(filme));
    }

    /** Não há exclusão física: o filme fica inativo e os aluguéis ativos continuam valendo. */
    @Transactional
    public void inativar(Long id) {
        buscarEntidade(id).inativar();
    }

    @Transactional
    public FilmeResponse reativar(Long id) {
        Filme filme = buscarEntidade(id);
        filme.reativar();
        return FilmeResponse.de(filme);
    }

    private Filme buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));
    }

    private void garantirTituloUnico(String titulo, Long idAtual) {
        String normalizado = Filme.normalizarTitulo(titulo);
        boolean existe = idAtual == null
                ? repository.existsByTituloNormalizado(normalizado)
                : repository.existsByTituloNormalizadoAndIdNot(normalizado, idAtual);
        if (existe) {
            throw new ConflitoException("Já existe um filme com o título \"" + titulo.trim() + "\".");
        }
    }

    private static void aplicar(Filme filme, FilmeRequest dados) {
        filme.setTitulo(dados.titulo());
        filme.setDuracaoMinutos(dados.duracaoMinutos());
        filme.setLicencasDisponiveis(dados.licencasDisponiveis());
    }
}
