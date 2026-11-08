package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.*;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsoMaquinaService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final MaquinaRepository maquinaRepository;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;
    private final UsoMaquinaRepository usoMaquinaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<UsoMaquinaDTO.UsoMaquinaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return usoMaquinaRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public UsoMaquinaDTO.UsoMaquinaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public UsoMaquinaDTO.UsoMaquinaResponse cadastrar(UsoMaquinaDTO.UsoMaquinaRequest request) {
        contexto.bloquearFazenda();

        UsoMaquina entidade = new UsoMaquina();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = usoMaquinaRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "UsoMaquina", entidade.getUsoMaquinaId());
        return converterParaResponse(entidade);
    }

    private UsoMaquina buscarEntidadePorId(Long id) {
        UsoMaquina entidade =
                usoMaquinaRepository
                        .findByUsoMaquinaIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "UsoMaquina não encontrado nesta fazenda."));

        return entidade;
    }

    private UsoMaquinaDTO.UsoMaquinaResponse converterParaResponse(UsoMaquina entidade) {
        UsoMaquinaDTO.UsoMaquinaResponse response = new UsoMaquinaDTO.UsoMaquinaResponse();
        response.setUsoMaquinaId(entidade.getUsoMaquinaId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodMaquina(
                entidade.getMaquina() == null ? null : entidade.getMaquina().getMaquinaId());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setLeitura(entidade.getLeitura());
        response.setIntervaloUso(entidade.getIntervaloUso());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodTarefa(
                entidade.getTarefa() == null ? null : entidade.getTarefa().getTarefaId());
        return response;
    }

    private void aplicarRequestNaEntidade(
            UsoMaquina entidade, UsoMaquinaDTO.UsoMaquinaRequest request) {
        entidade.setMaquina(
                request.getCodMaquina() == null
                        ? null
                        : maquinaRepository
                                .findById(request.getCodMaquina())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Maquina inexistente ou de outra fazenda.")));
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setLeitura(request.getLeitura());
        entidade.setSafra(
                request.getCodSafra() == null
                        ? null
                        : safraRepository
                                .findById(request.getCodSafra())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(() -> erro("Safra inexistente ou de outra fazenda.")));
        entidade.setLote(
                request.getCodLote() == null
                        ? null
                        : loteRepository
                                .findById(request.getCodLote())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(() -> erro("Lote inexistente ou de outra fazenda.")));
        entidade.setTarefa(
                request.getCodTarefa() == null
                        ? null
                        : tarefaRepository
                                .findById(request.getCodTarefa())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Tarefa inexistente ou de outra fazenda.")));
        Maquina maquina =
                maquinaRepository.bloquear(entidade.getMaquina().getMaquinaId()).orElseThrow();
        if (!"HORAS".equals(maquina.getUnidadeMedidor()))
            throw erro("Registro de horas exige horímetro.");
        validarDestino(entidade.getSafra(), entidade.getLote(), entidade.getTarefa());
        if (maquina.getDtaLeitura() != null
                && entidade.getDtaOperacao().isBefore(maquina.getDtaLeitura()))
            throw erro("Data anterior à última leitura.");
        if (entidade.getLeitura().compareTo(maquina.getLeitura()) < 0)
            throw erro("Leitura menor que a anterior.");
        entidade.setIntervaloUso(entidade.getLeitura().subtract(maquina.getLeitura()));
        maquina.setLeitura(entidade.getLeitura());
        maquina.setDtaLeitura(entidade.getDtaOperacao());
    }

    private void validarDestino(Safra safra, Lote lote, Tarefa tarefa) {
        if (safra != null && lote != null) throw erro("Destinos simultâneos não permitidos.");
        if (tarefa != null) {
            if (safra != null
                    && (tarefa.getSafra() == null
                            || !safra.getSafraId().equals(tarefa.getSafra().getSafraId())))
                throw erro("Safra diverge da tarefa.");
            if (lote != null
                    && (tarefa.getLote() == null
                            || !lote.getLoteId().equals(tarefa.getLote().getLoteId())))
                throw erro("Lote diverge da tarefa.");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
