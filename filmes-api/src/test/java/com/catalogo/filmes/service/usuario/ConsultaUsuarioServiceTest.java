package com.catalogo.filmes.service.usuario;

import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.service.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultaUsuarioServiceTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final ConsultaUsuarioService service = new ConsultaUsuarioService(repository);

    @Test
    void buscarDevolveOsDadosSemASenha() {
        Usuario ana = new Usuario("Ana", "ana@teste.com", "hash", Perfil.CLIENTE, Instant.now());
        ReflectionTestUtils.setField(ana, "id", 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ana));

        UsuarioResponse resposta = service.buscar(1L);

        assertEquals(new UsuarioResponse(1L, "Ana", "ana@teste.com", Perfil.CLIENTE), resposta);
    }

    @Test
    void usuarioInexistenteDeveFalhar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(99L));
    }
}
