package com.catalogo.filmes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lê o cabeçalho {@code Authorization: Bearer}, valida o JWT e autentica o usuário.
 * Não é um bean de propósito: assim ele só roda dentro da cadeia do Spring Security.
 * Token ausente ou inválido não gera erro aqui; a regra da rota decide (401 ou acesso livre).
 */
public class SecurityFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioDetailsService usuarios;

    public SecurityFilter(TokenService tokenService, UsuarioDetailsService usuarios) {
        this.tokenService = tokenService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            tokenService.validar(cabecalho.substring(PREFIXO.length()).trim()).ifPresent(this::autenticar);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String email) {
        try {
            UsuarioLogado usuario = usuarios.loadUserByUsername(email);
            var autenticacao = UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacao);
            SecurityContextHolder.setContext(contexto);
        } catch (UsernameNotFoundException e) {
            // Conta removida depois do login: segue sem autenticação e a rota protegida responde 401.
        }
    }
}
