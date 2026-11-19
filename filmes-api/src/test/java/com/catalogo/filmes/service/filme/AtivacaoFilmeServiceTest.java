package com.catalogo.filmes.service.filme;

import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AtivacaoFilmeServiceTest {

    private final FilmeRepository repository = mock(FilmeRepository.class);
    private final AtivacaoFilmeService service = new AtivacaoFilmeService(repository);

    private Filme filme(boolean ativo) {
        Filme filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", 1L);
        filme.setTitulo("Matriz");
        filme.setDuracaoMinutos(120);
        filme.setLicencasDisponiveis(3);
        if (!ativo) {
            filme.inativar();
        }
        when(repository.findById(1L)).thenReturn(Optional.of(filme));
        return filme;
    }

    @Test
    void inativarDeveMarcarFilmeComoInativo() {
        Filme filme = filme(true);

        service.inativar(1L);

        assertFalse(filme.isAtivo());
    }

    @Test
    void reativarDeveVoltarOFilmeAoCatalogo() {
        Filme filme = filme(false);
        when(repository.saveAndFlush(any(Filme.class))).thenAnswer(i -> i.getArgument(0));

        FilmeResponse resposta = service.reativar(1L);

        assertTrue(resposta.ativo());
        assertTrue(filme.isAtivo());
    }
}
