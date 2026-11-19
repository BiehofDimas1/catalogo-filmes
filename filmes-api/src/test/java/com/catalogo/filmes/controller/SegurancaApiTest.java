package com.catalogo.filmes.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaApiTest extends ApiTestBase {

    @Test
    void visitanteSemLoginNaoVeOCatalogo() throws Exception {
        mvc.perform(get("/filmes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists());
        mvc.perform(get("/alugueis/meus")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoEhRecusado() throws Exception {
        mvc.perform(get("/filmes").header(AUTORIZACAO, bearer("token-invalido")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNaoGerenciaFilmes() throws Exception {
        String cliente = novoCliente("Ana");

        mvc.perform(post("/filmes").header(AUTORIZACAO, bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmeJson("Filme do cliente", 90, 1)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(put("/filmes/1").header(AUTORIZACAO, bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON).content(filmeJson("Outro", 90, 1)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/filmes/1").header(AUTORIZACAO, bearer(cliente)))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/filmes/1/reativar").header(AUTORIZACAO, bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoVeAlugueisDeTodosNemValidaCodigos() throws Exception {
        String cliente = novoCliente("Ana");

        mvc.perform(get("/alugueis").header(AUTORIZACAO, bearer(cliente)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/alugueis/acesso/qualquer").header(AUTORIZACAO, bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNaoAlugaNemTemHistoricoDeCliente() throws Exception {
        String admin = novoAdmin();
        long filmeId = criarFilme(admin, unico("Filme"), 1);

        mvc.perform(post("/alugueis").header(AUTORIZACAO, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(aluguelJson(filmeId)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/alugueis/meus").header(AUTORIZACAO, bearer(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoCancelaAluguelDeOutroCliente() throws Exception {
        String admin = novoAdmin();
        String ana = novoCliente("Ana");
        String bruno = novoCliente("Bruno");
        long filmeId = criarFilme(admin, unico("Filme"), 2);
        String aluguel = corpo(mvc.perform(post("/alugueis").header(AUTORIZACAO, bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(aluguelJson(filmeId)))
                .andExpect(status().isCreated()));
        long aluguelId = numero(aluguel, "$.id");

        mvc.perform(post("/alugueis/" + aluguelId + "/cancelar").header(AUTORIZACAO, bearer(bruno)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/alugueis/meus").header(AUTORIZACAO, bearer(bruno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void cadastroPublicoNuncaCriaAdmin() throws Exception {
        String email = novoEmail("esperto");

        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Esperto\",\"email\":\"" + email
                                + "\",\"senha\":\"senha123\",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void loginComSenhaErradaRecebe401() throws Exception {
        String email = novoEmail("ana");
        cadastrar("Ana", email, "senha123");

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "errada99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void emailRepetidoNoCadastroRecebe409() throws Exception {
        String email = novoEmail("ana");
        cadastrar("Ana", email, "senha123");

        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson("Ana 2", email.toUpperCase(), "senha123")))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroInvalidoRecebe400ComErroPorCampo() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson("", "nao-e-email", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.senha").exists());
    }

    @Test
    void usuarioLogadoConsultaOsProprioDados() throws Exception {
        String cliente = novoCliente("Ana");

        mvc.perform(get("/auth/me").header(AUTORIZACAO, bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void preflightDoAngularEhLiberadoPeloCors() throws Exception {
        mvc.perform(options("/filmes")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }
}
