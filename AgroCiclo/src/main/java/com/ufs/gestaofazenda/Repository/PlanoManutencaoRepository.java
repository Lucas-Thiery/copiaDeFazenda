package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.PlanoManutencao;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PlanoManutencaoRepository extends JpaRepository<PlanoManutencao, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PlanoManutencao e where e.planoManutencaoId = :id")
    Optional<PlanoManutencao> bloquear(@Param("id") Long id);

    List<PlanoManutencao> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<PlanoManutencao> findAllByFazendaFazendaId(Long codFazenda);

    Optional<PlanoManutencao> findByPlanoManutencaoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
