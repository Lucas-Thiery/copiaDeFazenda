package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.VersaoPlanejamento;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface VersaoPlanejamentoRepository extends JpaRepository<VersaoPlanejamento, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from VersaoPlanejamento e where e.versaoPlanejamentoId = :id")
    Optional<VersaoPlanejamento> bloquear(@Param("id") Long id);

    List<VersaoPlanejamento> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<VersaoPlanejamento> findAllByFazendaFazendaId(Long codFazenda);

    Optional<VersaoPlanejamento> findByVersaoPlanejamentoIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
