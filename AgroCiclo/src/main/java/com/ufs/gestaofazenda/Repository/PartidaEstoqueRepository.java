package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.PartidaEstoque;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PartidaEstoqueRepository extends JpaRepository<PartidaEstoque, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PartidaEstoque e where e.partidaEstoqueId = :id")
    Optional<PartidaEstoque> bloquear(@Param("id") Long id);

    List<PartidaEstoque> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<PartidaEstoque> findAllByFazendaFazendaId(Long codFazenda);

    Optional<PartidaEstoque> findByPartidaEstoqueIdAndFazendaFazendaId(Long id, Long codFazenda);
}
