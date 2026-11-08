package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.PlanoManejo;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PlanoManejoRepository extends JpaRepository<PlanoManejo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PlanoManejo e where e.planoManejoId = :id")
    Optional<PlanoManejo> bloquear(@Param("id") Long id);

    List<PlanoManejo> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<PlanoManejo> findAllByFazendaFazendaId(Long codFazenda);

    Optional<PlanoManejo> findByPlanoManejoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
