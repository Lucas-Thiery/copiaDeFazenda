package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.VersaoLimites;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface VersaoLimitesRepository extends JpaRepository<VersaoLimites, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from VersaoLimites e where e.versaoLimitesId = :id")
    Optional<VersaoLimites> bloquear(@Param("id") Long id);

    List<VersaoLimites> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<VersaoLimites> findAllByFazendaFazendaId(Long codFazenda);

    Optional<VersaoLimites> findByVersaoLimitesIdAndFazendaFazendaId(Long id, Long codFazenda);
}
