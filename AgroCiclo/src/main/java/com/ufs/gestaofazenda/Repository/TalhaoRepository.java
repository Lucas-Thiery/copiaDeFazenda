package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Talhao;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TalhaoRepository extends JpaRepository<Talhao, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Talhao e where e.talhaoId = :id")
    Optional<Talhao> bloquear(@Param("id") Long id);

    List<Talhao> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Talhao> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Talhao> findByTalhaoIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
