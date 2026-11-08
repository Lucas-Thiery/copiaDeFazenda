package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Pastagem;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PastagemRepository extends JpaRepository<Pastagem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Pastagem e where e.pastagemId = :id")
    Optional<Pastagem> bloquear(@Param("id") Long id);

    List<Pastagem> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Pastagem> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Pastagem> findByPastagemIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
