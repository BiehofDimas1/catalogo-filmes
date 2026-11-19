package com.catalogo.filmes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class SecurityFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final VerificadorDeToken verificadorDeToken;
    private final UserDetailsService usuarios;

    public SecurityFilter(VerificadorDeToken verificadorDeToken, UserDetailsService usuarios) {
        this.verificadorDeToken = verificadorDeToken;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        tokenDaRequisicao(request)
                .flatMap(verificadorDeToken::verificar)
                .ifPresent(this::autenticar);
        chain.doFilter(request, response);
    }

    private static Optional<String> tokenDaRequisicao(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO)) {
            return Optional.empty();
        }
        return Optional.of(cabecalho.substring(PREFIXO.length()).trim());
    }

    private void autenticar(String email) {
        try {
            UserDetails usuario = usuarios.loadUserByUsername(email);
            var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                    usuario, null, usuario.getAuthorities());
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacao);
            SecurityContextHolder.setContext(contexto);
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
        }
    }
}
