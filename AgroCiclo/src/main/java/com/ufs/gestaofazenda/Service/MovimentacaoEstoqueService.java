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
public class MovimentacaoEstoqueService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FuncionarioRepository funcionarioRepository;
    private final LoteRepository loteRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final PartidaEstoqueRepository partidaEstoqueRepository;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse> listar(
            int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return movimentacaoEstoqueRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .peek(
                        r -> {
                            if (!contexto.proprietario()) r.setValorTotalOrigem(null);
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse buscarPorId(Long id) {
        var response = converterParaResponse(buscarEntidadePorId(id));
        if (!contexto.proprietario()) response.setValorTotalOrigem(null);
        return response;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse cadastrar(
            MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequest request) {
        return registrar(request, false);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse consumirAplicacao(
            MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequest request) {
        return registrar(request, true);
    }

    private MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse registrar(
            MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequest request, boolean aplicacao) {
        contexto.bloquearFazenda();

        MovimentacaoEstoque entidade = new MovimentacaoEstoque();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request, aplicacao);
        entidade = movimentacaoEstoqueRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "MovimentacaoEstoque", entidade.getMovimentacaoEstoqueId());
        return converterParaResponse(entidade);
    }

    private MovimentacaoEstoque buscarEntidadePorId(Long id) {
        MovimentacaoEstoque entidade =
                movimentacaoEstoqueRepository
                        .findByMovimentacaoEstoqueIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "MovimentacaoEstoque não encontrado nesta"
                                                    + " fazenda."));

        return entidade;
    }

    private MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse converterParaResponse(
            MovimentacaoEstoque entidade) {
        MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse response =
                new MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse();
        response.setMovimentacaoEstoqueId(entidade.getMovimentacaoEstoqueId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodPartida(
                entidade.getPartida() == null ? null : entidade.getPartida().getPartidaEstoqueId());
        response.setTipo(entidade.getTipo());
        response.setQuantidade(entidade.getQuantidade());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setMotivo(entidade.getMotivo());
        response.setValorTotalOrigem(entidade.getValorTotalOrigem());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodTarefa(
                entidade.getTarefa() == null ? null : entidade.getTarefa().getTarefaId());
        response.setReferenciaOrigem(entidade.getReferenciaOrigem());
        response.setCodResponsavel(
                entidade.getResponsavel() == null
                        ? null
                        : entidade.getResponsavel().getFuncionarioId());
        return response;
    }

    private void aplicarRequestNaEntidade(
            MovimentacaoEstoque entidade,
            MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequest request,
            boolean aplicacao) {
        entidade.setPartida(
                request.getCodPartida() == null
                        ? null
                        : partidaEstoqueRepository
                                .findById(request.getCodPartida())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "PartidaEstoque inexistente ou de outra"
                                                            + " fazenda.")));
        entidade.setTipo(request.getTipo().trim());
        entidade.setQuantidade(request.getQuantidade());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setMotivo(request.getMotivo().trim());
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
        entidade.setReferenciaOrigem(request.getReferenciaOrigem());
        entidade.setResponsavel(
                request.getCodResponsavel() == null
                        ? null
                        : funcionarioRepository
                                .findById(request.getCodResponsavel())
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
        if (entidade.getTipo().equals("ENTRADA"))
            throw erro("Use o endpoint de entradas por partida.");
        PartidaEstoque partida =
                partidaEstoqueRepository
                        .bloquear(entidade.getPartida().getPartidaEstoqueId())
                        .orElseThrow();
        if (!partida.getItem().isAtivo()) throw erro("Item inativo.");
        if (!request.getUnidade().equals(partida.getItem().getUnidadeMedida()))
            throw erro("Unidade incompatível.");
        if (entidade.getReferenciaOrigem() == null || entidade.getReferenciaOrigem().isBlank())
            throw erro("Informe a origem da saída.");
        if (movimentacaoEstoqueRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(m -> entidade.getReferenciaOrigem().equals(m.getReferenciaOrigem())))
            throw erro("Origem de saída já registrada.");
        if (entidade.getDtaOperacao().isBefore(partida.getDtaEntrada()))
            throw erro("Saída anterior à entrada da partida.");
        if (entidade.getQuantidade().compareTo(partida.getQuantidadeAtual()) > 0)
            throw erro("Saldo insuficiente.");
        if (entidade.getTipo().equals("USO")) {
            if (entidade.getResponsavel() == null)
                throw erro("Informe o responsável pelo consumo.");
            if (!aplicacao
                    && Set.of("VACINA", "MEDICAMENTO", "DEFENSIVO", "INSUMO")
                            .contains(partida.getItem().getCategoria()))
                throw erro(
                        "Aplicações agrícolas e sanitárias devem ser registradas em"
                            + " /api/aplicacoes.");
            if (partida.getDtaValidade() == null)
                throw erro("Validade pendente; uso não liberado.");
            if (entidade.getDtaOperacao().isAfter(partida.getDtaValidade()))
                throw erro("Partida vencida: uso bloqueado.");
            if (entidade.getSafra() == null
                    && entidade.getLote() == null
                    && entidade.getTarefa() == null) throw erro("Consumo exige destino.");
            if (partida.getValorUnitarioOrigem() == null) throw erro("Origem sem preço.");
        }
        validarDestino(entidade.getSafra(), entidade.getLote(), entidade.getTarefa());
        if (entidade.getTipo().equals("DESCARTE")
                && (entidade.getSafra() != null
                        || entidade.getLote() != null
                        || entidade.getTarefa() != null))
            throw erro("Descarte não é consumo de atividade.");
        partida.setQuantidadeAtual(partida.getQuantidadeAtual().subtract(entidade.getQuantidade()));
        entidade.setValorTotalOrigem(
                entidade.getQuantidade().multiply(partida.getValorUnitarioOrigem()));
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
