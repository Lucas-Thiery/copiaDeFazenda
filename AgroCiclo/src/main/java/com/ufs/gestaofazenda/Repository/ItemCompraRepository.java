package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.ItemCompra;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ItemCompraRepository extends JpaRepository<ItemCompra, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ItemCompra e where e.itemCompraId = :id")
    Optional<ItemCompra> bloquear(@Param("id") Long id);

    List<ItemCompra> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<ItemCompra> findAllByFazendaFazendaId(Long codFazenda);

    Optional<ItemCompra> findByItemCompraIdAndFazendaFazendaId(Long id, Long codFazenda);
}
