package com.catalogo.filmes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "filmes")
@Getter
@Setter
@NoArgsConstructor
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @NotNull(message = "A duração é obrigatória")
    @Min(value = 1, message = "A duração mínima é 1 minuto")
    @Max(value = 600, message = "A duração máxima é 600 minutos")
    private Integer duracaoMinutos;

    private boolean ativo = true;

    @Min(value = 0, message = "As licenças não podem ser negativas")
    private int licencasDisponiveis;
}