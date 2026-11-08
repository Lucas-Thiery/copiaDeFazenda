package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.ItemEstoque;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ItemEstoque e where e.itemEstoqueId = :id")
    Optional<ItemEstoque> bloquear(@Param("id") Long id);

    List<ItemEstoque> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<ItemEstoque> findAllByFazendaFazendaId(Long codFazenda);

    Optional<ItemEstoque> findByItemEstoqueIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndNomeIgnoreCase(Long codFazenda, String valor);
}
