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
public class ManutencaoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LancamentoRepository lancamentoRepository;
    private final LancamentoService lancamentoService;
    private final LoteRepository loteRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MaquinaRepository maquinaRepository;
    private final PlanoManutencaoRepository planoManutencaoRepository;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<ManutencaoDTO.ManutencaoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return manutencaoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ManutencaoDTO.ManutencaoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ManutencaoDTO.ManutencaoResponse cadastrar(ManutencaoDTO.ManutencaoRequest request) {
        contexto.bloquearFazenda();

        Manutencao entidade = new Manutencao();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = manutencaoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Manutencao", entidade.getManutencaoId());
        return converterParaResponse(entidade);
    }

    private Manutencao buscarEntidadePorId(Long id) {
        Manutencao entidade =
                manutencaoRepository
                        .findByManutencaoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Manutencao não encontrado nesta fazenda."));

        return entidade;
    }

    private ManutencaoDTO.ManutencaoResponse converterParaResponse(Manutencao entidade) {
        ManutencaoDTO.ManutencaoResponse response = new ManutencaoDTO.ManutencaoResponse();
        response.setManutencaoId(entidade.getManutencaoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodMaquina(
                entidade.getMaquina() == null ? null : entidade.getMaquina().getMaquinaId());
        response.setCodPlano(
                entidade.getPlano() == null ? null : entidade.getPlano().getPlanoManutencaoId());
        response.setTipo(entidade.getTipo());
        response.setServico(entidade.getServico());
        response.setPecas(entidade.getPecas());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setCusto(entidade.getCusto());
        response.setReferencia(entidade.getReferencia());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodTarefa(
                entidade.getTarefa() == null ? null : entidade.getTarefa().getTarefaId());
        response.setCodLancamento(
                entidade.getLancamento() == null
                        ? null
                        : entidade.getLancamento().getLancamentoId());
        return response;
    }

    private void aplicarRequestNaEntidade(
            Manutencao entidade, ManutencaoDTO.ManutencaoRequest request) {
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
        entidade.setPlano(
                request.getCodPlano() == null
                        ? null
                        : planoManutencaoRepository
                                .findById(request.getCodPlano())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "PlanoManutencao inexistente ou de outra"
                                                            + " fazenda.")));
        entidade.setTipo(request.getTipo().trim());
        entidade.setServico(request.getServico().trim());
        entidade.setPecas(request.getPecas());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setCusto(request.getCusto());
        entidade.setReferencia(request.getReferencia().trim());
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
        entidade.setLancamento(
                request.getCodLancamento() == null
                        ? null
                        : lancamentoRepository
                                .findById(request.getCodLancamento())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Lancamento inexistente ou de outra fazenda.")));
        validarDestino(entidade.getSafra(), entidade.getLote(), entidade.getTarefa());
        Maquina maquina =
                maquinaRepository.bloquear(entidade.getMaquina().getMaquinaId()).orElseThrow();
        if (entidade.getPlano() != null) {
            PlanoManutencao plano =
                    planoManutencaoRepository
                            .bloquear(entidade.getPlano().getPlanoManutencaoId())
                            .orElseThrow();
            if (plano.isConcluido())
                throw erro("Plano já concluído; consulte a manutenção existente.");
            if (!plano.getMaquina().getMaquinaId().equals(maquina.getMaquinaId())
                    || !entidade.getTipo().equals("PREVENTIVA")) throw erro("Plano incompatível.");
            plano.setConcluido(true);
        }
        if (entidade.getCusto().signum() > 0)
            entidade.setLancamento(
                    lancamentoService.origemMaquina(
                            entidade.getLancamento(),
                            entidade.getReferencia(),
                            "MANUTENCAO",
                            entidade.getCusto(),
                            entidade.getDtaOperacao(),
                            entidade.getSafra(),
                            entidade.getLote(),
                            entidade.getTarefa()));
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
