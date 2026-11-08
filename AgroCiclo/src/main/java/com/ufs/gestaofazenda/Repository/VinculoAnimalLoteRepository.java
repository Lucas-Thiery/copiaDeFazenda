package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.VinculoAnimalLote;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface VinculoAnimalLoteRepository extends JpaRepository<VinculoAnimalLote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from VinculoAnimalLote e where e.vinculoAnimalLoteId = :id")
    Optional<VinculoAnimalLote> bloquear(@Param("id") Long id);

    List<VinculoAnimalLote> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<VinculoAnimalLote> findAllByFazendaFazendaId(Long codFazenda);

    Optional<VinculoAnimalLote> findByVinculoAnimalLoteIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
