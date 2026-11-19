package com.catalogo.filmes.security;

import com.catalogo.filmes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UsuarioLogado loadUserByUsername(String email) {
        return repository.findByEmail(email)
                .map(UsuarioLogado::de)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }
}
