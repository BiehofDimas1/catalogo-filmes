package com.catalogo.filmes.security;

import com.catalogo.filmes.model.Perfil;
import com.catalogo.filmes.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuário autenticado na requisição atual, recarregado do banco a cada chamada. */
public record UsuarioLogado(Long id, String nome, String email, String senha, Perfil perfil) implements UserDetails {

    public static UsuarioLogado de(Usuario usuario) {
        return new UsuarioLogado(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getSenha(), usuario.getPerfil());
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /** Não inclui o hash da senha, para ele nunca aparecer em logs. */
    @Override
    public String toString() {
        return "UsuarioLogado[id=" + id + ", email=" + email + ", perfil=" + perfil + "]";
    }
}
