package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.ProducaoPecuaria;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProducaoPecuariaRepository extends JpaRepository<ProducaoPecuaria, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ProducaoPecuaria e where e.producaoPecuariaId = :id")
    Optional<ProducaoPecuaria> bloquear(@Param("id") Long id);

    List<ProducaoPecuaria> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<ProducaoPecuaria> findAllByFazendaFazendaId(Long codFazenda);

    Optional<ProducaoPecuaria> findByProducaoPecuariaIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
