package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Manejo;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ManejoRepository extends JpaRepository<Manejo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Manejo e where e.manejoId = :id")
    Optional<Manejo> bloquear(@Param("id") Long id);

    List<Manejo> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Manejo> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Manejo> findByManejoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
