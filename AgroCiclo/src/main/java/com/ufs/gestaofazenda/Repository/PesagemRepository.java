package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Pesagem;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PesagemRepository extends JpaRepository<Pesagem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Pesagem e where e.pesagemId = :id")
    Optional<Pesagem> bloquear(@Param("id") Long id);

    List<Pesagem> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Pesagem> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Pesagem> findByPesagemIdAndFazendaFazendaId(Long id, Long codFazenda);
}
