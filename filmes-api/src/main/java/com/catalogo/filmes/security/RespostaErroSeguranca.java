package com.catalogo.filmes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 401 (sem login) e 403 (perfil sem permissão) no mesmo formato ProblemDetail
 * usado pelo restante da API, para a interface mostrar a mensagem.
 */
@Component
public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

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

    private static void escrever(HttpServletRequest request, HttpServletResponse response, int status,
                                 String titulo, String detalhe) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + json(titulo)
                + "\",\"status\":" + status
                + ",\"detail\":\"" + json(detalhe)
                + "\",\"instance\":\"" + json(request.getRequestURI()) + "\"}");
    }

    private static String json(String texto) {
        StringBuilder saida = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> saida.append("\\\"");
                case '\\' -> saida.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        saida.append(String.format("\\u%04x", (int) c));
                    } else {
                        saida.append(c);
                    }
                }
            }
        }
        return saida.toString();
    }
}
