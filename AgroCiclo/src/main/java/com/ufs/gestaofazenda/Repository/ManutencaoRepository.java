package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Manutencao;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Manutencao e where e.manutencaoId = :id")
    Optional<Manutencao> bloquear(@Param("id") Long id);

    List<Manutencao> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Manutencao> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Manutencao> findByManutencaoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
