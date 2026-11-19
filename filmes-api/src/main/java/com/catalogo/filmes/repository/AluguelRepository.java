package com.catalogo.filmes.repository;

import com.catalogo.filmes.model.Aluguel;
import com.catalogo.filmes.model.StatusAluguel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    boolean existsByUsuarioIdAndFilmeIdAndStatusAndDataExpiracaoAfter(
            Long usuarioId, Long filmeId, StatusAluguel status, Instant instante);

    long countByUsuarioIdAndStatusAndDataExpiracaoAfter(
            Long usuarioId, StatusAluguel status, Instant instante);

    List<Aluguel> findByFilmeIdAndStatusAndDataExpiracaoLessThanEqual(
            Long filmeId, StatusAluguel status, Instant instante);

    @Query("select a.id from Aluguel a where a.status = :status and a.dataExpiracao <= :instante")
    List<Long> buscarIdsVencidos(@Param("status") StatusAluguel status, @Param("instante") Instant instante);

    @EntityGraph(attributePaths = {"filme", "usuario"})
    List<Aluguel> findByUsuarioIdOrderByDataAluguelDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"filme", "usuario"})
    List<Aluguel> findAllByOrderByDataAluguelDesc();

    @EntityGraph(attributePaths = {"filme", "usuario"})
    Optional<Aluguel> findByCodigoAcesso(String codigoAcesso);
}
