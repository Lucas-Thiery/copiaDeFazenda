package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.ProducaoColheita;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProducaoColheitaRepository extends JpaRepository<ProducaoColheita, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ProducaoColheita e where e.producaoColheitaId = :id")
    Optional<ProducaoColheita> bloquear(@Param("id") Long id);

    List<ProducaoColheita> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<ProducaoColheita> findAllByFazendaFazendaId(Long codFazenda);

    Optional<ProducaoColheita> findByProducaoColheitaIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
