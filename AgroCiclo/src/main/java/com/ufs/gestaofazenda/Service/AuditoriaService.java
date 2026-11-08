package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.AuditoriaDTO;
import com.ufs.gestaofazenda.Entity.Auditoria;
import com.ufs.gestaofazenda.Repository.AuditoriaRepository;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;
    private final ContextoUsuario contexto;
    private final HttpServletRequest request;

    public void registrar(String tipo, String entidade, Long id) {
        Auditoria evento = new Auditoria();
        evento.setCodFazenda(contexto.codFazenda());
        evento.setAutor(contexto.usuario().getLogin());
        evento.setTipoEvento(tipo);
        evento.setEntidade(entidade);
        evento.setCodRegistro(id);
        evento.setSolicitacao(request.getHeader("Idempotency-Key"));
        auditoriaRepository.save(evento);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public List<AuditoriaDTO.AuditoriaResponse> consultar(
            LocalDate inicio, LocalDate fim, String operacao) {
        if (fim.isBefore(inicio))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido.");
        return auditoriaRepository
                .findByCodFazendaAndDtaOperacaoBetweenOrderByDtaOperacaoDesc(
                        contexto.codFazenda(),
                        inicio.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(),
                        fim.plusDays(1)
                                .atStartOfDay(ZoneId.of("America/Sao_Paulo"))
                                .toInstant()
                                .minusNanos(1))
                .stream()
                .filter(a -> operacao == null || a.getTipoEvento().equals(operacao))
                .map(this::converterParaResponse)
                .toList();
    }

    private AuditoriaDTO.AuditoriaResponse converterParaResponse(Auditoria auditoria) {
        var response = new AuditoriaDTO.AuditoriaResponse();
        response.setAuditoriaId(auditoria.getAuditoriaId());
        response.setCodFazenda(auditoria.getCodFazenda());
        response.setAutor(auditoria.getAutor());
        response.setOperacao(auditoria.getTipoEvento());
        response.setEntidade(auditoria.getEntidade());
        response.setCodRegistro(auditoria.getCodRegistro());
        response.setDtaOperacao(auditoria.getDtaOperacao());
        response.setSolicitacao(auditoria.getSolicitacao());
        return response;
    }
}
