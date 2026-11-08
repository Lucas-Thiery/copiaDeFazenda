package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Lancamento;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Lancamento e where e.lancamentoId = :id")
    Optional<Lancamento> bloquear(@Param("id") Long id);

    List<Lancamento> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Lancamento> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Lancamento> findByLancamentoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
