package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Liquidacao;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface LiquidacaoRepository extends JpaRepository<Liquidacao, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Liquidacao e where e.liquidacaoId = :id")
    Optional<Liquidacao> bloquear(@Param("id") Long id);

    List<Liquidacao> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Liquidacao> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Liquidacao> findByLiquidacaoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
