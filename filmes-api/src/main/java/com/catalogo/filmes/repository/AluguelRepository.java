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

    /** Existe aluguel ativo (e ainda não vencido) do usuário para o filme? */
    boolean existsByUsuarioIdAndFilmeIdAndStatusAndDataExpiracaoAfter(
            Long usuarioId, Long filmeId, StatusAluguel status, Instant instante);

    /** Quantidade de aluguéis ativos (e ainda não vencidos) do usuário. */
    long countByUsuarioIdAndStatusAndDataExpiracaoAfter(Long usuarioId, StatusAluguel status, Instant instante);

    /** Aluguéis do filme que ainda estão ativos, mas já venceram. */
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
