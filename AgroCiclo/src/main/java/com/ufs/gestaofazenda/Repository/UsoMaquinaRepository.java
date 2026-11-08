package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.UsoMaquina;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UsoMaquinaRepository extends JpaRepository<UsoMaquina, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from UsoMaquina e where e.usoMaquinaId = :id")
    Optional<UsoMaquina> bloquear(@Param("id") Long id);

    List<UsoMaquina> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<UsoMaquina> findAllByFazendaFazendaId(Long codFazenda);

    Optional<UsoMaquina> findByUsoMaquinaIdAndFazendaFazendaId(Long id, Long codFazenda);
}
