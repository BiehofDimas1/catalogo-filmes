package com.catalogo.filmes.service.filme;

import com.catalogo.filmes.dto.FilmeRequest;
import com.catalogo.filmes.dto.FilmeResponse;
import com.catalogo.filmes.model.Filme;
import com.catalogo.filmes.repository.FilmeRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import com.catalogo.filmes.service.exception.VersaoDesatualizadaException;
import com.catalogo.filmes.service.filme.validacao.TituloUnicoValidador;
import com.catalogo.filmes.service.filme.validacao.ValidadorFilme;
import com.catalogo.filmes.service.filme.validacao.VersaoAtualValidador;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CadastroFilmeServiceTest {

    private final FilmeRepository repository = mock(FilmeRepository.class);

    private List<ValidadorFilme> validadoresDoSistema() {
        return List.of(new VersaoAtualValidador(), new TituloUnicoValidador(repository));
    }

    private CadastroFilmeService servico(List<ValidadorFilme> validadores) {
        return new CadastroFilmeService(repository, validadores);
    }

    private CadastroFilmeService servico() {
        return servico(validadoresDoSistema());
    }

    private Filme filmeSalvo(Long id, String titulo) {
        Filme filme = new Filme();
        ReflectionTestUtils.setField(filme, "id", id);
        ReflectionTestUtils.setField(filme, "versao", 0L);
        filme.setTitulo(titulo);
        filme.setDuracaoMinutos(120);
        filme.setLicencasDisponiveis(3);
        return filme;
    }

    @Test
    void cadastrarDeveSalvarFilmeAtivo() {
        when(repository.existsByTituloNormalizado("interestelar")).thenReturn(false);
        when(repository.save(any(Filme.class))).thenAnswer(i -> i.getArgument(0));

        FilmeResponse resposta = servico().cadastrar(new FilmeRequest("Interestelar", 169, 3, null));

        assertEquals("Interestelar", resposta.titulo());
        assertEquals(169, resposta.duracaoMinutos());
        assertEquals(3, resposta.licencasDisponiveis());
        assertTrue(resposta.ativo());
    }

    @Test
    void cadastrarTituloRepetidoSemDiferenciarMaiusculasDeveFalhar() {
        when(repository.existsByTituloNormalizado("matriz")).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> servico().cadastrar(new FilmeRequest("  MATRIZ ", 120, 2, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void atualizarParaTituloDeOutroFilmeDeveFalhar() {
        when(repository.findById(1L)).thenReturn(Optional.of(filmeSalvo(1L, "Matriz")));
        when(repository.existsByTituloNormalizadoAndIdNot("avatar", 1L)).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> servico().atualizar(1L, new FilmeRequest("Avatar", 160, 1, 0L)));
    }

    @Test
    void atualizarComVersaoDesatualizadaDeveFalharSemMexerNasLicencas() {
        Filme filme = filmeSalvo(1L, "Matriz");
        ReflectionTestUtils.setField(filme, "versao", 1L);
        filme.setLicencasDisponiveis(2);
        when(repository.findById(1L)).thenReturn(Optional.of(filme));

        assertThrows(VersaoDesatualizadaException.class,
                () -> servico().atualizar(1L, new FilmeRequest("Matriz", 150, 3, 0L)));

        assertEquals(2, filme.getLicencasDisponiveis());
        assertEquals(120, filme.getDuracaoMinutos());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizarMantendoOProprioTituloDevePassar() {
        when(repository.findById(1L)).thenReturn(Optional.of(filmeSalvo(1L, "Matriz")));
        when(repository.existsByTituloNormalizadoAndIdNot("matriz", 1L)).thenReturn(false);
        when(repository.saveAndFlush(any(Filme.class))).thenAnswer(i -> i.getArgument(0));

        FilmeResponse resposta = servico().atualizar(1L, new FilmeRequest("Matriz", 136, 5, 0L));

        assertEquals(136, resposta.duracaoMinutos());
        assertEquals(5, resposta.licencasDisponiveis());
    }

    @Test
    void regraNovaDeFilmeEntraSemMudarOServico() {
        List<ValidadorFilme> validadores = new ArrayList<>(validadoresDoSistema());
        validadores.add(pedido -> {
            throw new RegraNegocioException("Cadastro de filmes fechado para manutenção.");
        });

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico(validadores).cadastrar(new FilmeRequest("Duna", 155, 1, null)));

        assertEquals("Cadastro de filmes fechado para manutenção.", erro.getMessage());
        verify(repository, never()).save(any());
    }
}
