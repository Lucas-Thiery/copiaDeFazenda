package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByCodFazendaAndDtaOperacaoBetweenOrderByDtaOperacaoDesc(
            Long codFazenda, Instant inicio, Instant fim);
}
