package com.catalogo.filmes.security;

import java.util.Optional;

public interface VerificadorDeToken {

    Optional<String> verificar(String token);
}
