package com.catalogo.filmes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

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
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    @Setter(AccessLevel.NONE)
    @Column(name = "titulo_normalizado", nullable = false, unique = true, length = 150)
    private String tituloNormalizado;

    @NotNull(message = "A duração é obrigatória")
    @Min(value = 1, message = "A duração mínima é 1 minuto")
    @Max(value = 600, message = "A duração máxima é 600 minutos")
    @Column(nullable = false)
    private Integer duracaoMinutos;

    @Column(nullable = false)
    private boolean ativo = true;

    @Min(value = 0, message = "As licenças não podem ser negativas")
    @Column(nullable = false)
    private int licencasDisponiveis;

    @Version
    @Setter(AccessLevel.NONE)
    private Long versao;

    public void setTitulo(String titulo) {
        this.titulo = titulo == null ? null : titulo.trim();
        this.tituloNormalizado = normalizarTitulo(titulo);
    }

    public static String normalizarTitulo(String titulo) {
        if (titulo == null) {
            return null;
        }
        return titulo.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public boolean temLicencaDisponivel() {
        return licencasDisponiveis > 0;
    }

    public void retirarLicenca() {
        if (!temLicencaDisponivel()) {
            throw new IllegalStateException("Não há licenças disponíveis para o filme " + titulo);
        }
        licencasDisponiveis--;
    }

    public void devolverLicenca() {
        licencasDisponiveis++;
    }

    public void inativar() {
        ativo = false;
    }

    public void reativar() {
        ativo = true;
    }
}
