package com.catalogo.filmes.service.filme;

import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaFilmeServiceTest {

    private final FilmeRepository repository = mock(FilmeRepository.class);
    private final ConsultaFilmeService service = new ConsultaFilmeService(repository);

    private Filme criarFilme(Long id, String titulo, boolean ativo) {
        Filme filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", id);
        filme.setTitulo(titulo);
        filme.setDuracaoMinutos(120);
        filme.setLicencasDisponiveis(3);
        if (!ativo) {
            filme.inativar();
        }
        return filme;
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
