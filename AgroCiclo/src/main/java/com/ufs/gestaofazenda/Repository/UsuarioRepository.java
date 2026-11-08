package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Usuario;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Usuario e where e.usuarioId = :id")
    Optional<Usuario> bloquear(@Param("id") Long id);

    @EntityGraph(attributePaths = {"funcionario", "funcionario.fazenda", "fazendas"})
    Optional<Usuario> findByLoginIgnoreCase(String login);

    boolean existsByFuncionarioFuncionarioId(Long codFuncionario);

    List<Usuario> findDistinctByFazendasFazendaId(Long codFazenda);
}
