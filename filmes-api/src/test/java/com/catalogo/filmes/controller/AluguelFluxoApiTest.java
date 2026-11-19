package com.catalogo.filmes.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AluguelFluxoApiTest extends ApiTestBase {

    private String alugar(String token, long filmeId, int statusEsperado) throws Exception {
        return corpo(mvc.perform(post("/alugueis").header(AUTORIZACAO, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(aluguelJson(filmeId)))
                .andExpect(status().is(statusEsperado)));
    }

    @Test
    void fluxoCompletoComCancelamentoReembolsoEValidacaoDeAcesso() throws Exception {
        String admin = novoAdmin();
        String ana = novoCliente("Ana");
        String bruno = novoCliente("Bruno");
        long filmeId = criarFilme(admin, unico("Matriz"), 1);

        assertTrue(ids(consultar("/filmes", ana)).contains(filmeId));

        String aluguel = alugar(ana, filmeId, 201);
        long aluguelId = numero(aluguel, "$.id");
        String codigo = JsonPath.read(aluguel, "$.codigoAcesso");
        mvc.perform(get("/filmes/" + filmeId).header(AUTORIZACAO, bearer(ana)))
                .andExpect(jsonPath("$.licencasDisponiveis").value(0));
        assertEquals("ATIVO", JsonPath.read(aluguel, "$.status"));
        assertTrue(JsonPath.<Boolean>read(aluguel, "$.cancelavel"));

        alugar(ana, filmeId, 409);
        alugar(bruno, filmeId, 422);

        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/alugueis/acesso/" + codigo).header(AUTORIZACAO, bearer(admin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").value(true))
                    .andExpect(jsonPath("$.status").value("ATIVO"));
        }

        mvc.perform(post("/alugueis/" + aluguelId + "/cancelar").header(AUTORIZACAO, bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.transacaoReembolso").exists());
        mvc.perform(post("/alugueis/" + aluguelId + "/cancelar").header(AUTORIZACAO, bearer(ana)))
                .andExpect(status().is(422));
        mvc.perform(get("/alugueis/acesso/" + codigo).header(AUTORIZACAO, bearer(admin)))
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        alugar(bruno, filmeId, 201);

        mvc.perform(get("/alugueis/meus").header(AUTORIZACAO, bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CANCELADO"));
        assertTrue(ids(consultar("/alugueis", admin)).contains(aluguelId));

        mvc.perform(get("/alugueis/acesso/codigo-que-nao-existe").header(AUTORIZACAO, bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void tituloDuplicadoSemDiferenciarMaiusculasRecebe409() throws Exception {
        String admin = novoAdmin();
        String titulo = unico("Avatar");
        criarFilme(admin, titulo, 1);

        mvc.perform(post("/filmes").header(AUTORIZACAO, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmeJson("  " + titulo.toUpperCase() + " ", 162, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void edicaoComVersaoDesatualizadaRecebe409ENaoDesfazOAluguel() throws Exception {
        String admin = novoAdmin();
        String ana = novoCliente("Ana");
        String titulo = unico("Tropa");
        long filmeId = criarFilme(admin, titulo, 1);

        String aberto = consultar("/filmes/" + filmeId, admin);
        long versaoAberta = numero(aberto, "$.versao");

        alugar(ana, filmeId, 201);

        mvc.perform(put("/filmes/" + filmeId).header(AUTORIZACAO, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmeJson(titulo, 130, 1, versaoAberta)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de atualização"));
        String depois = corpo(mvc.perform(get("/filmes/" + filmeId).header(AUTORIZACAO, bearer(admin)))
                .andExpect(jsonPath("$.licencasDisponiveis").value(0))
                .andExpect(jsonPath("$.duracaoMinutos").value(120)));

        long versaoAtual = numero(depois, "$.versao");
        mvc.perform(put("/filmes/" + filmeId).header(AUTORIZACAO, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmeJson(titulo, 130, 0, versaoAtual)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracaoMinutos").value(130))
                .andExpect(jsonPath("$.licencasDisponiveis").value(0))
                .andExpect(jsonPath("$.versao").value(versaoAtual + 1));
    }

    @Test
    void edicaoSemVersaoRecebe400ComMensagemDoCampo() throws Exception {
        String admin = novoAdmin();
        String titulo = unico("SemVersao");
        long filmeId = criarFilme(admin, titulo, 1);

        mvc.perform(put("/filmes/" + filmeId).header(AUTORIZACAO, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(filmeJson(titulo, 100, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.versao").exists());
    }

    @Test
    void duracaoAcimaDe600MinutosRecebe400ComMensagemDoCampo() throws Exception {
        String admin = novoAdmin();

        mvc.perform(post("/filmes").header(AUTORIZACAO, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(filmeJson(unico("Longo"), 601, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.duracaoMinutos").exists());
    }

    @Test
    void filmeInativoSaiDoCatalogoDoClienteEPodeSerReativado() throws Exception {
        String admin = novoAdmin();
        String ana = novoCliente("Ana");
        long filmeId = criarFilme(admin, unico("Duna"), 1);

        mvc.perform(delete("/filmes/" + filmeId).header(AUTORIZACAO, bearer(admin)))
                .andExpect(status().isNoContent());

        assertFalse(ids(consultar("/filmes", ana)).contains(filmeId));
        assertTrue(ids(consultar("/filmes", admin)).contains(filmeId));
        alugar(ana, filmeId, 422);

        mvc.perform(patch("/filmes/" + filmeId + "/reativar").header(AUTORIZACAO, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        alugar(ana, filmeId, 201);
    }

    @Test
    void clienteTemNoMaximoCincoAlugueisAtivos() throws Exception {
        String admin = novoAdmin();
        String ana = novoCliente("Ana");
        List<Long> filmes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            filmes.add(criarFilme(admin, unico("Filme"), 1));
        }

        for (int i = 0; i < 5; i++) {
            alugar(ana, filmes.get(i), 201);
        }
        String erro = alugar(ana, filmes.get(5), 422);
        assertTrue(JsonPath.<String>read(erro, "$.detail").contains("5"));
    }

    @Test
    void regrasDoAluguelFicamDisponiveisParaATela() throws Exception {
        String ana = novoCliente("Ana");

        mvc.perform(get("/alugueis/regras").header(AUTORIZACAO, bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(9.9))
                .andExpect(jsonPath("$.duracaoHoras").value(48))
                .andExpect(jsonPath("$.prazoCancelamentoMinutos").value(60))
                .andExpect(jsonPath("$.limiteAtivos").value(5));
    }
}
