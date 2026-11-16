package com.catalogo.filmes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados que o ADMIN envia para cadastrar ou atualizar um filme. */
public record FilmeRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @NotNull(message = "A duração é obrigatória.")
        @Min(value = 1, message = "A duração mínima é 1 minuto.")
        @Max(value = 600, message = "A duração máxima é 600 minutos.")
        Integer duracaoMinutos,

        @NotNull(message = "Informe as licenças disponíveis.")
        @Min(value = 0, message = "As licenças não podem ser negativas.")
        Integer licencasDisponiveis) {
}
