package com.catalogo.filmes.service;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import com.catalogo.filmes.service.exception.VersaoDesatualizadaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
        garantirVersaoAtual(filme, dados.versao());
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
        // saveAndFlush: a resposta já sai com a versão nova.
        return FilmeResponse.de(repository.saveAndFlush(filme));
    }

    private Filme buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado."));
    }

    /**
     * A edição traz a versão do filme que o ADMIN carregou no formulário. Se um aluguel, cancelamento ou
     * expiração mudou o filme depois disso, gravar o formulário desfaria a contagem de licenças.
     * Uma mudança simultânea, entre esta checagem e a gravação, é barrada pelo {@code @Version} e também vira 409.
     */
    private static void garantirVersaoAtual(Filme filme, Long versaoCarregada) {
        if (!Objects.equals(versaoCarregada, filme.getVersao())) {
            throw new VersaoDesatualizadaException("Este filme mudou depois que você abriu o formulário "
                    + "(por exemplo, por um aluguel). Recarregue a página para ver os dados atuais e salve de novo.");
        }
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
