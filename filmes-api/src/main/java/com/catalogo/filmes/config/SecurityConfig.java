package com.catalogo.filmes.config;

import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.security.RespostaErroSeguranca;
import com.catalogo.filmes.security.SecurityFilter;
import com.catalogo.filmes.security.VerificadorDeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = Perfil.ADMIN.name();
    private static final String CLIENTE = Perfil.CLIENTE.name();
    private static final String[] FILMES = {"/filmes", "/filmes/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, VerificadorDeToken verificadorDeToken,
                                                   UserDetailsService usuarios,
                                                   RespostaErroSeguranca respostaErro) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(cabecalhos -> cabecalhos.frameOptions(frames -> frames.sameOrigin()))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostaErro)
                        .accessDeniedHandler(respostaErro))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro", "/auth/login").permitAll()
                        .requestMatchers("/h2-console/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, FILMES).hasAnyRole(ADMIN, CLIENTE)
                        .requestMatchers(FILMES).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/alugueis").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/alugueis/meus").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.POST, "/alugueis/*/cancelar").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/alugueis", "/alugueis/acesso/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .addFilterBefore(new SecurityFilter(verificadorDeToken, usuarios),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
