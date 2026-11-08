package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Fazenda;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FazendaRepository extends JpaRepository<Fazenda, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Fazenda e where e.fazendaId = :id")
    Optional<Fazenda> bloquear(@Param("id") Long id);
}
