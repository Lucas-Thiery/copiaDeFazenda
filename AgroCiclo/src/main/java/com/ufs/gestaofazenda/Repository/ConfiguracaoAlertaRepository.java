package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.ConfiguracaoAlerta;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ConfiguracaoAlertaRepository extends JpaRepository<ConfiguracaoAlerta, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ConfiguracaoAlerta e where e.configuracaoAlertaId = :id")
    Optional<ConfiguracaoAlerta> bloquear(@Param("id") Long id);

    List<ConfiguracaoAlerta> findAllByFazendaFazendaId(Long codFazenda, Pageable pageable);

    List<ConfiguracaoAlerta> findAllByFazendaFazendaId(Long codFazenda);

    Optional<ConfiguracaoAlerta> findByConfiguracaoAlertaIdAndFazendaFazendaId(
            Long id, Long codFazenda);
}
