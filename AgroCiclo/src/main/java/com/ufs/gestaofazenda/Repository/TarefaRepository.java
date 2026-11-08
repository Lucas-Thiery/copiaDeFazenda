package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Tarefa;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Tarefa e where e.tarefaId = :id")
    Optional<Tarefa> bloquear(@Param("id") Long id);

    List<Tarefa> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Tarefa> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Tarefa> findByTarefaIdAndFazendaFazendaId(Long id, Long codFazenda);
}
