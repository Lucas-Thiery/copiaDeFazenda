package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.EtapaCultivo;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface EtapaCultivoRepository extends JpaRepository<EtapaCultivo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EtapaCultivo e where e.etapaCultivoId = :id")
    Optional<EtapaCultivo> bloquear(@Param("id") Long id);

    List<EtapaCultivo> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<EtapaCultivo> findAllByFazendaFazendaId(Long codFazenda);

    Optional<EtapaCultivo> findByEtapaCultivoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
