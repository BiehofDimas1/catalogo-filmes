package com.catalogo.filmes.dto;

import jakarta.validation.constraints.NotNull;

public record AluguelRequest(@NotNull(message = "Informe o filme.") Long filmeId) {
}
