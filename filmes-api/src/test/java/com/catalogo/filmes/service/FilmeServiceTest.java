package com.catalogo.filmes.service;

import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmeServiceTest {

    private final FilmeRepository repository = Mockito.mock(FilmeRepository.class);
    private final FilmeService service = new FilmeService(repository);

    private Filme criarFilme(String titulo) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setDuracaoMinutos(120);
        f.setLicencasDisponiveis(3);
        return f;
    }

    @Test
    void listarDeveRetornarOsFilmes() {
        Mockito.when(repository.findAll())
                .thenReturn(List.of(criarFilme("Matriz"), criarFilme("Avatar")));

        List<Filme> filmes = service.listar();

        assertEquals(2, filmes.size());
        assertEquals("Matriz", filmes.get(0).getTitulo());
    }

    @Test
    void inativarDeveMarcarFilmeComoInativo() {
        Filme filme = criarFilme("Matriz");
        Mockito.when(repository.findById(1L)).thenReturn(java.util.Optional.of(filme));
        Mockito.when(repository.save(Mockito.any(Filme.class))).thenAnswer(i -> i.getArgument(0));

        service.inativar(1L);

        assertTrue(!filme.isAtivo());
        assertFalse(filme.isAtivo());
    }
}