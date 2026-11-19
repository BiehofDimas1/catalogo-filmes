package com.catalogo.filmes.config;

import com.catalogo.filmes.service.exception.ErroDaAplicacao;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void erroNovoUsaOProprioStatusSemMudarOHandler() {
        ErroDaAplicacao erroNovo = new ErroDaAplicacao("Muitas tentativas. Aguarde um minuto.") {
            @Override
            public int status() {
                return 429;
            }

            @Override
            public String titulo() {
                return "Limite de tentativas";
            }
        };

        ResponseEntity<ProblemDetail> resposta = handler.tratarErroDaAplicacao(erroNovo);

        assertEquals(429, resposta.getStatusCode().value());
        assertEquals("Limite de tentativas", resposta.getBody().getTitle());
        assertEquals("Muitas tentativas. Aguarde um minuto.", resposta.getBody().getDetail());
    }
}
