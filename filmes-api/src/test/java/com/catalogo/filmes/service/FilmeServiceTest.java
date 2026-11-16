package com.catalogo.filmes.service;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilmeServiceTest {

    private final FilmeRepository repository = Mockito.mock(FilmeRepository.class);
    private final FilmeService service = new FilmeService(repository);

    private Filme criarFilme(Long id, String titulo, boolean ativo) {
        Filme f = new Filme();
        ReflectionTestUtils.setField(f, "id", id);
        f.setTitulo(titulo);
        f.setDuracaoMinutos(120);
        f.setLicencasDisponiveis(3);
        if (!ativo) {
            f.inativar();
        }
        return f;
    }

    @Test
    void clienteVeSomenteOsFilmesAtivos() {
        when(repository.findByAtivoTrueOrderByTituloNormalizadoAsc())
                .thenReturn(List.of(criarFilme(1L, "Avatar", true), criarFilme(2L, "Matriz", true)));

        List<FilmeResponse> filmes = service.listar(false);

        assertEquals(2, filmes.size());
        assertEquals("Avatar", filmes.get(0).titulo());
        verify(repository, never()).findAllByOrderByTituloNormalizadoAsc();
    }

    @Test
    void adminVeTambemOsFilmesInativos() {
        when(repository.findAllByOrderByTituloNormalizadoAsc())
                .thenReturn(List.of(criarFilme(1L, "Avatar", true), criarFilme(2L, "Matriz", false)));

        List<FilmeResponse> filmes = service.listar(true);

        assertEquals(2, filmes.size());
        assertFalse(filmes.get(1).ativo());
    }

    @Test
    void cadastrarDeveSalvarFilmeAtivo() {
        when(repository.existsByTituloNormalizado("interestelar")).thenReturn(false);
        when(repository.save(any(Filme.class))).thenAnswer(i -> i.getArgument(0));

        FilmeResponse resposta = service.cadastrar(new FilmeRequest("Interestelar", 169, 3));

        assertEquals("Interestelar", resposta.titulo());
        assertEquals(169, resposta.duracaoMinutos());
        assertEquals(3, resposta.licencasDisponiveis());
        assertTrue(resposta.ativo());
    }

    @Test
    void cadastrarTituloRepetidoSemDiferenciarMaiusculasDeveFalhar() {
        when(repository.existsByTituloNormalizado("matriz")).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.cadastrar(new FilmeRequest("  MATRIZ ", 120, 2)));
        verify(repository, never()).save(any());
    }

    @Test
    void atualizarParaTituloDeOutroFilmeDeveFalhar() {
        when(repository.findById(1L)).thenReturn(Optional.of(criarFilme(1L, "Matriz", true)));
        when(repository.existsByTituloNormalizadoAndIdNot("avatar", 1L)).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.atualizar(1L, new FilmeRequest("Avatar", 160, 1)));
    }

    @Test
    void atualizarMantendoOProprioTituloDevePassar() {
        Filme filme = criarFilme(1L, "Matriz", true);
        when(repository.findById(1L)).thenReturn(Optional.of(filme));
        when(repository.existsByTituloNormalizadoAndIdNot("matriz", 1L)).thenReturn(false);
        when(repository.saveAndFlush(any(Filme.class))).thenAnswer(i -> i.getArgument(0));

        FilmeResponse resposta = service.atualizar(1L, new FilmeRequest("Matriz", 136, 5));

        assertEquals(136, resposta.duracaoMinutos());
        assertEquals(5, resposta.licencasDisponiveis());
    }

    @Test
    void inativarDeveMarcarFilmeComoInativo() {
        Filme filme = criarFilme(1L, "Matriz", true);
        when(repository.findById(1L)).thenReturn(Optional.of(filme));

        service.inativar(1L);

        assertFalse(filme.isAtivo());
    }

    @Test
    void reativarDeveVoltarOFilmeAoCatalogo() {
        Filme filme = criarFilme(1L, "Matriz", false);
        when(repository.findById(1L)).thenReturn(Optional.of(filme));

        FilmeResponse resposta = service.reativar(1L);

        assertTrue(resposta.ativo());
        assertTrue(filme.isAtivo());
    }

    @Test
    void clienteNaoEncontraFilmeInativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(criarFilme(1L, "Matriz", false)));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(1L, false));
        assertFalse(service.buscar(1L, true).ativo());
    }

    @Test
    void buscarFilmeInexistenteDeveFalhar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(99L, true));
    }
}
