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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

abstract class ApiTestBase {

    protected static final String AUTORIZACAO = "Authorization";
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    protected static String unico(String prefixo) {
        return prefixo + "-" + System.nanoTime() + "-" + SEQUENCIA.incrementAndGet();
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String corpo(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    protected static long numero(String json, String caminho) {
        return ((Number) JsonPath.read(json, caminho)).longValue();
    }

    protected static List<Long> ids(String json) {
        List<Number> ids = JsonPath.read(json, "$[*].id");
        return ids.stream().map(Number::longValue).toList();
    }

    protected static String filmeJson(String titulo, int duracao, int licencas) {
        return "{\"titulo\":\"" + titulo + "\",\"duracaoMinutos\":" + duracao
                + ",\"licencasDisponiveis\":" + licencas + "}";
    }

    protected static String filmeJson(String titulo, int duracao, int licencas, long versao) {
        return "{\"titulo\":\"" + titulo + "\",\"duracaoMinutos\":" + duracao
                + ",\"licencasDisponiveis\":" + licencas + ",\"versao\":" + versao + "}";
    }

    protected static String aluguelJson(long filmeId) {
        return "{\"filmeId\":" + filmeId + "}";
    }

    protected static String cadastroJson(String nome, String email, String senha) {
        return "{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    protected static String loginJson(String email, String senha) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    protected String novoEmail(String nome) {
        return unico(nome.toLowerCase()) + "@teste.com";
    }

    protected void cadastrar(String nome, String email, String senha) throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(nome, email, senha)))
                .andExpect(status().isCreated());
    }

    protected String login(String email, String senha) throws Exception {
        String resposta = corpo(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, senha)))
                .andExpect(status().isOk()));
        return JsonPath.read(resposta, "$.token");
    }

    protected String consultar(String url, String token) throws Exception {
        return corpo(mvc.perform(get(url).header(AUTORIZACAO, bearer(token)))
                .andExpect(status().isOk()));
    }

    protected String novoCliente(String nome) throws Exception {
        String email = novoEmail(nome);
        cadastrar(nome, email, "senha123");
        return login(email, "senha123");
    }

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
        return numero(resposta, "$.id");
    }
}
