package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Compra;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Compra e where e.compraId = :id")
    Optional<Compra> bloquear(@Param("id") Long id);

    List<Compra> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Compra> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Compra> findByCompraIdAndFazendaFazendaId(Long id, Long codFazenda);
}
