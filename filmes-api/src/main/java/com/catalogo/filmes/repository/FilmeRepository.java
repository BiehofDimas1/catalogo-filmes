package com.catalogo.filmes.repository;

import com.catalogo.filmes.model.Filme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmeRepository extends JpaRepository<Filme, Long> {

    List<Filme> findAllByOrderByTituloNormalizadoAsc();

    List<Filme> findByAtivoTrueOrderByTituloNormalizadoAsc();

    boolean existsByTituloNormalizado(String tituloNormalizado);

    boolean existsByTituloNormalizadoAndIdNot(String tituloNormalizado, Long id);
}
