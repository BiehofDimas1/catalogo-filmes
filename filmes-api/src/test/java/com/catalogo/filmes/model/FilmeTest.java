package com.catalogo.filmes.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmeTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Filme criarFilme(String titulo, Integer duracao) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setDuracaoMinutos(duracao);
        f.setLicencasDisponiveis(5);
        return f;
    }

    @Test
    void duracaoMaiorQue600DeveFalhar() {
        var erros = validator.validate(criarFilme("Filme longo", 700));
        assertEquals(1, erros.size());
    }

    @Test
    void duracaoZeroDeveFalhar() {
        var erros = validator.validate(criarFilme("Filme curto", 0));
        assertEquals(1, erros.size());
    }

    @Test
    void duracaoValidaDevePassar() {
        var erros = validator.validate(criarFilme("Interestelar", 169));
        assertTrue(erros.isEmpty());
    }
}