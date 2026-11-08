package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.MovimentacaoPastagem;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface MovimentacaoPastagemRepository extends JpaRepository<MovimentacaoPastagem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MovimentacaoPastagem e where e.movimentacaoPastagemId = :id")
    Optional<MovimentacaoPastagem> bloquear(@Param("id") Long id);

    List<MovimentacaoPastagem> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<MovimentacaoPastagem> findAllByFazendaFazendaId(Long codFazenda);

    Optional<MovimentacaoPastagem> findByMovimentacaoPastagemIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
