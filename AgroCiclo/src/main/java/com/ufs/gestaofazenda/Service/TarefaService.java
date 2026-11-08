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
public class TarefaService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FuncionarioRepository funcionarioRepository;
    private final LoteRepository loteRepository;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<TarefaDTO.TarefaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return tarefaRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .filter(
                        e ->
                                !contexto.operador()
                                        || e.getExecutante()
                                                .getFuncionarioId()
                                                .equals(
                                                        contexto.usuario()
                                                                .getFuncionario()
                                                                .getFuncionarioId()))
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public TarefaDTO.TarefaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public TarefaDTO.TarefaResponse cadastrar(TarefaDTO.TarefaRequest request) {
        contexto.bloquearFazenda();

        Tarefa entidade = new Tarefa();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = tarefaRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Tarefa", entidade.getTarefaId());
        return converterParaResponse(entidade);
    }

    private Tarefa buscarEntidadePorId(Long id) {
        Tarefa entidade =
                tarefaRepository
                        .findByTarefaIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Tarefa não encontrado nesta fazenda."));
        if (contexto.operador()
                && !entidade.getExecutante()
                        .getFuncionarioId()
                        .equals(contexto.usuario().getFuncionario().getFuncionarioId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tarefa de outro Operador.");
        return entidade;
    }

    private TarefaDTO.TarefaResponse converterParaResponse(Tarefa entidade) {
        TarefaDTO.TarefaResponse response = new TarefaDTO.TarefaResponse();
        response.setTarefaId(entidade.getTarefaId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodExecutante(
                entidade.getExecutante() == null
                        ? null
                        : entidade.getExecutante().getFuncionarioId());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setAtividade(entidade.getAtividade());
        response.setLocal(entidade.getLocal());
        response.setDtaInicioPrevisto(entidade.getDtaInicioPrevisto());
        response.setDtaFimPrevisto(entidade.getDtaFimPrevisto());
        response.setStatus(entidade.getStatus());
        response.setDtaInicioReal(entidade.getDtaInicioReal());
        response.setDtaFimReal(entidade.getDtaFimReal());
        response.setTaxaPreservada(contexto.proprietario() ? entidade.getTaxaPreservada() : null);
        response.setDuracaoHoras(contexto.proprietario() ? entidade.getDuracaoHoras() : null);
        response.setCustoMaoObra(contexto.proprietario() ? entidade.getCustoMaoObra() : null);
        response.setObservacoes(entidade.getObservacoes());
        return response;
    }

    private void aplicarRequestNaEntidade(Tarefa entidade, TarefaDTO.TarefaRequest request) {
        entidade.setExecutante(
                request.getCodExecutante() == null
                        ? null
                        : funcionarioRepository
                                .findById(request.getCodExecutante())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "Funcionario inexistente ou de outra"
                                                            + " fazenda.")));
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
        entidade.setAtividade(request.getAtividade().trim());
        entidade.setLocal(request.getLocal().trim());
        entidade.setDtaInicioPrevisto(request.getDtaInicioPrevisto());
        entidade.setDtaFimPrevisto(request.getDtaFimPrevisto());
        entidade.setObservacoes(request.getObservacoes());
        validarDestino(entidade.getSafra(), entidade.getLote(), null);
        if ((entidade.getSafra() == null) == (entidade.getLote() == null))
            throw erro("Tarefa exige safra ou lote.");
        Usuario operador =
                usuarioRepository.findAll().stream()
                        .filter(
                                u ->
                                        u.isAtivo()
                                                && u.getPerfil() == Perfil.OPERADOR
                                                && u.getFuncionario()
                                                        .getFuncionarioId()
                                                        .equals(
                                                                entidade.getExecutante()
                                                                        .getFuncionarioId()))
                        .findFirst()
                        .orElseThrow(() -> erro("Funcionário sem conta ativa de Operador."));
        funcionarioRepository.bloquear(entidade.getExecutante().getFuncionarioId()).orElseThrow();
        if (!entidade.getDtaFimPrevisto().isAfter(entidade.getDtaInicioPrevisto()))
            throw erro("Intervalo inválido.");
        if (tarefaRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        t ->
                                t.getExecutante()
                                                .getFuncionarioId()
                                                .equals(entidade.getExecutante().getFuncionarioId())
                                        && t.getDtaInicioPrevisto()
                                                .isBefore(entidade.getDtaFimPrevisto())
                                        && t.getDtaFimPrevisto()
                                                .isAfter(entidade.getDtaInicioPrevisto())))
            throw erro("Conflito de horário com tarefa atribuída.");
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

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','OPERADOR')")
    public TarefaDTO.TarefaResponse concluir(
            Long id, ExecucaoTarefaDTO.ExecucaoTarefaRequest request) {
        contexto.bloquearFazenda();
        Tarefa tarefa = buscarEntidadePorId(id);
        if (tarefa.getStatus().equals("CONCLUIDA")) return converterParaResponse(tarefa);
        if (!request.getDtaFim().isAfter(request.getDtaInicio())
                || request.getDtaFim().isAfter(LocalDateTime.now()))
            throw erro("Intervalo real inválido.");
        tarefa.setDtaInicioReal(request.getDtaInicio());
        tarefa.setDtaFimReal(request.getDtaFim());
        BigDecimal duracao =
                BigDecimal.valueOf(
                                Duration.between(request.getDtaInicio(), request.getDtaFim())
                                        .toSeconds())
                        .divide(BigDecimal.valueOf(3600), 8, RoundingMode.HALF_UP);
        tarefa.setDuracaoHoras(duracao);
        tarefa.setTaxaPreservada(tarefa.getExecutante().getCustoHoraEfetivo());
        tarefa.setCustoMaoObra(
                duracao.multiply(tarefa.getTaxaPreservada()).setScale(4, RoundingMode.HALF_UP));
        tarefa.setObservacoes(request.getObservacoes());
        tarefa.setStatus("CONCLUIDA");
        auditoriaService.registrar("CONCLUSAO", "Tarefa", id);
        return converterParaResponse(tarefa);
    }
}
