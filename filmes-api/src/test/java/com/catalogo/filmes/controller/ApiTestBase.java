package com.catalogo.filmes.controller;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Apoio para os testes de API: cadastro, login, promoção de ADMIN e criação de filmes. */
abstract class ApiTestBase {

    protected static final String AUTORIZACAO = "Authorization";
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    /** Nome único, para os testes não colidirem no banco compartilhado. */
    protected static String unico(String prefixo) {
        return prefixo + "-" + System.nanoTime() + "-" + SEQUENCIA.incrementAndGet();
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String corpo(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    protected static List<Long> ids(String json) {
        List<Number> ids = JsonPath.read(json, "$[*].id");
        return ids.stream().map(Number::longValue).toList();
    }

    protected static String filmeJson(String titulo, int duracao, int licencas) {
        return "{\"titulo\":\"" + titulo + "\",\"duracaoMinutos\":" + duracao
                + ",\"licencasDisponiveis\":" + licencas + "}";
    }

    protected static String aluguelJson(long filmeId) {
        return "{\"filmeId\":" + filmeId + "}";
    }

    protected String novoEmail(String nome) {
        return unico(nome.toLowerCase()) + "@teste.com";
    }

    protected void cadastrar(String nome, String email, String senha) throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isCreated());
    }

    protected String login(String email, String senha) throws Exception {
        String resposta = corpo(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()));
        return JsonPath.read(resposta, "$.token");
    }

    protected String novoCliente(String nome) throws Exception {
        String email = novoEmail(nome);
        cadastrar(nome, email, "senha123");
        return login(email, "senha123");
    }

    /** Cria um ADMIN pelo mesmo caminho documentado no README: cadastro comum + SQL no console do H2. */
    protected String novoAdmin() throws Exception {
        String email = novoEmail("admin");
        cadastrar("Admin", email, "senha123");
        jdbc.update("update usuarios set perfil = 'ADMIN' where email = ?", email);
        return login(email, "senha123");
    }

    protected long criarFilme(String tokenAdmin, String titulo, int licencas) throws Exception {
        String resposta = corpo(mvc.perform(post("/filmes").header(AUTORIZACAO, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmeJson(titulo, 120, licencas)))
                .andExpect(status().isCreated()));
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }
}
