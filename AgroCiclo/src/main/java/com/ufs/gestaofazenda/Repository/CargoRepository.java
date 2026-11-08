package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Cargo;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Cargo e where e.cargoId = :id")
    Optional<Cargo> bloquear(@Param("id") Long id);

    List<Cargo> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Cargo> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Cargo> findByCargoIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndNomeIgnoreCase(Long codFazenda, String valor);
}
