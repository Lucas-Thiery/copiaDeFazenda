package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.AgendamentoSanitario;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AgendamentoSanitarioRepository extends JpaRepository<AgendamentoSanitario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from AgendamentoSanitario e where e.agendamentoSanitarioId = :id")
    Optional<AgendamentoSanitario> bloquear(@Param("id") Long id);

    List<AgendamentoSanitario> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<AgendamentoSanitario> findAllByFazendaFazendaId(Long codFazenda);

    Optional<AgendamentoSanitario> findByAgendamentoSanitarioIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
