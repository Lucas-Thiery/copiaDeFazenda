package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.MovimentacaoEstoque;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MovimentacaoEstoque e where e.movimentacaoEstoqueId = :id")
    Optional<MovimentacaoEstoque> bloquear(@Param("id") Long id);

    List<MovimentacaoEstoque> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<MovimentacaoEstoque> findAllByFazendaFazendaId(Long codFazenda);

    Optional<MovimentacaoEstoque> findByMovimentacaoEstoqueIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
