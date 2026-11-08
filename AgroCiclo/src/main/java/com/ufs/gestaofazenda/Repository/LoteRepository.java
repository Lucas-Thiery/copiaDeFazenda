package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Lote;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Lote e where e.loteId = :id")
    Optional<Lote> bloquear(@Param("id") Long id);

    List<Lote> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Lote> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Lote> findByLoteIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
