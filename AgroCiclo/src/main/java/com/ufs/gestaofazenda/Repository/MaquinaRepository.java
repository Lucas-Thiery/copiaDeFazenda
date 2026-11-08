package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Maquina;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Maquina e where e.maquinaId = :id")
    Optional<Maquina> bloquear(@Param("id") Long id);

    List<Maquina> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Maquina> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Maquina> findByMaquinaIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
