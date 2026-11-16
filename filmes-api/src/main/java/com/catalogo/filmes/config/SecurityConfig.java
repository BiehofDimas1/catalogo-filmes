package com.catalogo.filmes.config;

import com.catalogo.filmes.security.RespostaErroSeguranca;
import com.catalogo.filmes.security.SecurityFilter;
import com.catalogo.filmes.security.TokenService;
import com.catalogo.filmes.security.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Regras de acesso por perfil. A autorização é sempre decidida aqui e nos services;
 * os guards do Angular só controlam a navegação.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokenService,
                                                   UsuarioDetailsService usuarioDetailsService,
                                                   RespostaErroSeguranca respostaErro) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // O console do H2 usa frames da mesma origem.
                .headers(cabecalhos -> cabecalhos.frameOptions(frames -> frames.sameOrigin()))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostaErro)
                        .accessDeniedHandler(respostaErro))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro", "/auth/login").permitAll()
                        .requestMatchers("/h2-console/**", "/error").permitAll()
                        // Catálogo: qualquer usuário logado consulta; só o ADMIN altera.
                        .requestMatchers(HttpMethod.GET, "/filmes", "/filmes/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers("/filmes", "/filmes/**").hasRole("ADMIN")
                        // Aluguel: o CLIENTE aluga, vê o histórico e cancela os próprios.
                        .requestMatchers(HttpMethod.POST, "/alugueis").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/alugueis/meus").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/alugueis/*/cancelar").hasRole("CLIENTE")
                        // O ADMIN vê todos os aluguéis e valida códigos de acesso.
                        .requestMatchers(HttpMethod.GET, "/alugueis", "/alugueis/acesso/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new SecurityFilter(tokenService, usuarioDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origens:http://localhost:4200}") List<String> origens) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origens);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracao.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
