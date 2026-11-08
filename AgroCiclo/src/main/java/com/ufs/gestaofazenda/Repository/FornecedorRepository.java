package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Fornecedor;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Fornecedor e where e.fornecedorId = :id")
    Optional<Fornecedor> bloquear(@Param("id") Long id);

    List<Fornecedor> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Fornecedor> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Fornecedor> findByFornecedorIdAndFazendaFazendaId(Long id, Long codFazenda);

    boolean existsByFazendaFazendaIdAndIdentificacaoIgnoreCase(Long codFazenda, String valor);
}
