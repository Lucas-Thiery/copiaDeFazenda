package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Funcionario;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Funcionario e where e.funcionarioId = :id")
    Optional<Funcionario> bloquear(@Param("id") Long id);

    List<Funcionario> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<Funcionario> findAllByFazendaFazendaId(Long codFazenda);

    Optional<Funcionario> findByFuncionarioIdAndFazendaFazendaId(Long id, Long codFazenda);
}
