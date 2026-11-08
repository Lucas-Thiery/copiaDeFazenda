package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Safra;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SafraRepository extends JpaRepository<Safra, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Safra e where e.safraId = :id")
    Optional<Safra> bloquear(@Param("id") Long id);

    List<Safra> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Safra> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Safra> findBySafraIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
