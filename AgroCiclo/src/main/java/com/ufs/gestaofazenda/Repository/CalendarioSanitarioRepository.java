package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.CalendarioSanitario;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CalendarioSanitarioRepository extends JpaRepository<CalendarioSanitario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CalendarioSanitario e where e.calendarioSanitarioId = :id")
    Optional<CalendarioSanitario> bloquear(@Param("id") Long id);

    List<CalendarioSanitario> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<CalendarioSanitario> findAllByFazendaFazendaId(Long codFazenda);

    Optional<CalendarioSanitario> findByCalendarioSanitarioIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
