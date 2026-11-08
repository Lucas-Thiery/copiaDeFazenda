package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Aplicacao;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AplicacaoRepository extends JpaRepository<Aplicacao, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Aplicacao e where e.aplicacaoId = :id")
    Optional<Aplicacao> bloquear(@Param("id") Long id);

    List<Aplicacao> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Aplicacao> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Aplicacao> findByAplicacaoIdAndFazendaFazendaId(Long id, Long codFazenda);
}
