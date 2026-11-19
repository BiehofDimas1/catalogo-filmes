package com.catalogo.filmes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper json;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException excecao) throws IOException {
        escrever(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado",
                "Faça login para acessar este recurso.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException excecao) throws IOException {
        escrever(request, response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado",
                "O seu perfil não tem permissão para esta operação.");
    }

    private void escrever(HttpServletRequest request, HttpServletResponse response, int status,
                          String titulo, String detalhe) throws IOException {
        Map<String, Object> problema = new LinkedHashMap<>();
        problema.put("type", "about:blank");
        problema.put("title", titulo);
        problema.put("status", status);
        problema.put("detail", detalhe);
        problema.put("instance", request.getRequestURI());
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/problem+json");
        json.writeValue(response.getWriter(), problema);
    }
}
