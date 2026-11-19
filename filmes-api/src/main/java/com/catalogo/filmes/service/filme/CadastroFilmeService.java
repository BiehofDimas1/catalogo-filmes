package com.catalogo.filmes.service.filme;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.filme.validacao.PedidoFilme;
import com.catalogo.filmes.service.filme.validacao.ValidadorFilme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CadastroFilmeService {

    private final FilmeRepository repository;
    private final List<ValidadorFilme> validadores;

    @Transactional
    public FilmeResponse cadastrar(FilmeRequest dados) {
        validar(PedidoFilme.cadastro(dados));
        Filme filme = new Filme();
        aplicar(filme, dados);
        return FilmeResponse.de(repository.save(filme));
    }

    @Transactional
    public FilmeResponse atualizar(Long id, FilmeRequest dados) {
        Filme filme = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));
        validar(PedidoFilme.edicao(filme, dados));
        aplicar(filme, dados);
        return FilmeResponse.de(repository.saveAndFlush(filme));
    }

    private void validar(PedidoFilme pedido) {
        validadores.forEach(validador -> validador.validar(pedido));
    }

    private static void aplicar(Filme filme, FilmeRequest dados) {
        filme.setTitulo(dados.titulo());
        filme.setDuracaoMinutos(dados.duracaoMinutos());
        filme.setLicencasDisponiveis(dados.licencasDisponiveis());
    }
}
