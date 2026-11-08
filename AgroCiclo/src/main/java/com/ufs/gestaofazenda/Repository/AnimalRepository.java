package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Animal;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Animal e where e.animalId = :id")
    Optional<Animal> bloquear(@Param("id") Long id);

    List<Animal> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Animal> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Animal> findByAnimalIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
