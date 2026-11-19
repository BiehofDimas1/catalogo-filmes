package com.catalogo.filmes.security;

import java.time.Instant;

public record TokenGerado(String token, Instant expiraEm) {
}
