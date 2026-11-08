package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Abastecimento;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Abastecimento e where e.abastecimentoId = :id")
    Optional<Abastecimento> bloquear(@Param("id") Long id);

    List<Abastecimento> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Abastecimento> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Abastecimento> findByAbastecimentoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
