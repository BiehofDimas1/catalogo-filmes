package com.catalogo.filmes.service.usuario;

import com.catalogo.filmes.dto.CadastroRequest;
import com.catalogo.filmes.dto.UsuarioResponse;
import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import com.catalogo.filmes.repository.UsuarioRepository;
import com.catalogo.filmes.service.exception.ConflitoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CadastroUsuarioServiceTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final CadastroUsuarioService service =
            new CadastroUsuarioService(repository, encoder, Clock.systemUTC());

    @Test
    void cadastroSempreCriaClienteComSenhaCriptografada() {
        when(repository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        CadastroRequest dados = new CadastroRequest(" Ana ", " Ana@Teste.com ", "senha123");

        UsuarioResponse resposta = service.cadastrar(dados);

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(salvo.capture());
        assertEquals(Perfil.CLIENTE, salvo.getValue().getPerfil());
        assertEquals("ana@teste.com", salvo.getValue().getEmail());
        assertEquals("Ana", salvo.getValue().getNome());
        assertNotEquals("senha123", salvo.getValue().getSenha());
        assertTrue(encoder.matches("senha123", salvo.getValue().getSenha()));
        assertEquals(Perfil.CLIENTE, resposta.perfil());
    }

    @Test
    void cadastroComEmailJaUsadoDeveFalhar() {
        when(repository.existsByEmail("ana@teste.com")).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.cadastrar(new CadastroRequest("Ana", "ANA@teste.com", "senha123")));
        verify(repository, never()).save(any());
    }
}
