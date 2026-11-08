package com.ufs.gestaofazenda.Repository;

import com.ufs.gestaofazenda.Entity.Solicitacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    Optional<Solicitacao> findByCodFazendaAndCodUsuarioAndChave(
            Long codFazenda, Long codUsuario, String chave);
}
