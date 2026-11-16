package com.catalogo.filmes.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regras de validação do formulário de filme (o que a API devolve como erro 400). */
class FilmeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(FilmeRequest dados) {
        Set<ConstraintViolation<FilmeRequest>> erros = validator.validate(dados);
        return erros.stream().map(e -> e.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void duracaoDe600MinutosEhOLimite() {
        assertTrue(camposInvalidos(new FilmeRequest("Filme longo", 600, 1)).isEmpty());
        assertEquals(Set.of("duracaoMinutos"), camposInvalidos(new FilmeRequest("Filme longo", 601, 1)));
    }

    @Test
    void duracaoZeroDeveFalhar() {
        assertEquals(Set.of("duracaoMinutos"), camposInvalidos(new FilmeRequest("Filme curto", 0, 1)));
    }

    @Test
    void tituloEmBrancoDeveFalhar() {
        assertEquals(Set.of("titulo"), camposInvalidos(new FilmeRequest("   ", 90, 1)));
    }

    @Test
    void licencasNegativasDevemFalhar() {
        assertEquals(Set.of("licencasDisponiveis"), camposInvalidos(new FilmeRequest("Matriz", 136, -1)));
    }

    @Test
    void camposObrigatoriosAusentesDevemFalhar() {
        assertEquals(Set.of("titulo", "duracaoMinutos", "licencasDisponiveis"),
                camposInvalidos(new FilmeRequest(null, null, null)));
    }
}
