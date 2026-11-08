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
public class AplicacaoService {
    private final AgendamentoSanitarioRepository agendamentoSanitarioRepository;
    private final AnimalRepository animalRepository;
    private final AplicacaoRepository aplicacaoRepository;
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FuncionarioRepository funcionarioRepository;
    private final LoteRepository loteRepository;
    private final ManejoRepository manejoRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final PartidaEstoqueRepository partidaEstoqueRepository;
    private final RebanhoService rebanhoService;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<AplicacaoDTO.AplicacaoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return aplicacaoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public AplicacaoDTO.AplicacaoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public AplicacaoDTO.AplicacaoResponse cadastrar(AplicacaoDTO.AplicacaoRequest request) {
        contexto.bloquearFazenda();

        Aplicacao entidade = new Aplicacao();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = aplicacaoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Aplicacao", entidade.getAplicacaoId());
        return converterParaResponse(entidade);
    }

    private Aplicacao buscarEntidadePorId(Long id) {
        Aplicacao entidade =
                aplicacaoRepository
                        .findByAplicacaoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Aplicacao não encontrado nesta fazenda."));

        return entidade;
    }

    private AplicacaoDTO.AplicacaoResponse converterParaResponse(Aplicacao entidade) {
        AplicacaoDTO.AplicacaoResponse response = new AplicacaoDTO.AplicacaoResponse();
        response.setAplicacaoId(entidade.getAplicacaoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodPartida(
                entidade.getPartida() == null ? null : entidade.getPartida().getPartidaEstoqueId());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setCodAnimal(
                entidade.getAnimal() == null ? null : entidade.getAnimal().getAnimalId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodTarefa(
                entidade.getTarefa() == null ? null : entidade.getTarefa().getTarefaId());
        response.setCodManejo(
                entidade.getManejo() == null ? null : entidade.getManejo().getManejoId());
        response.setCodAgendamento(
                entidade.getAgendamento() == null
                        ? null
                        : entidade.getAgendamento().getAgendamentoSanitarioId());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setQuantidade(entidade.getQuantidade());
        response.setUnidade(entidade.getUnidade());
        response.setCodResponsavel(
                entidade.getResponsavel() == null
                        ? null
                        : entidade.getResponsavel().getFuncionarioId());
        response.setDosagem(entidade.getDosagem());
        response.setOrientacaoTecnica(entidade.getOrientacaoTecnica());
        response.setCarenciaDias(entidade.getCarenciaDias());
        response.setCusto(contexto.proprietario() ? entidade.getCusto() : null);
        response.setReferencia(entidade.getReferencia());
        return response;
    }

    private void aplicarRequestNaEntidade(
            Aplicacao entidade, AplicacaoDTO.AplicacaoRequest request) {
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
        entidade.setAnimal(
                request.getCodAnimal() == null
                        ? null
                        : animalRepository
                                .findById(request.getCodAnimal())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Animal inexistente ou de outra fazenda.")));
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
        entidade.setManejo(
                request.getCodManejo() == null
                        ? null
                        : manejoRepository
                                .findById(request.getCodManejo())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Manejo inexistente ou de outra fazenda.")));
        entidade.setAgendamento(
                request.getCodAgendamento() == null
                        ? null
                        : agendamentoSanitarioRepository
                                .findById(request.getCodAgendamento())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "AgendamentoSanitario inexistente ou de"
                                                            + " outra fazenda.")));
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setQuantidade(request.getQuantidade());
        entidade.setUnidade(request.getUnidade().trim());
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
        entidade.setDosagem(request.getDosagem());
        entidade.setOrientacaoTecnica(request.getOrientacaoTecnica());
        entidade.setCarenciaDias(request.getCarenciaDias());
        entidade.setReferencia(request.getReferencia().trim());
        if (aplicacaoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(a -> a.getReferencia().equals(entidade.getReferencia())))
            throw erro("Aplicação já registrada para esta referência.");
        String categoria = entidade.getPartida().getItem().getCategoria();
        boolean sanitaria = categoria.equals("VACINA") || categoria.equals("MEDICAMENTO");
        if (entidade.getAnimal() != null) {
            if (entidade.getLote() != null || entidade.getSafra() != null)
                throw erro("Informe somente um destino.");
            entidade.setLote(
                    rebanhoService.loteNaData(entidade.getAnimal(), entidade.getDtaOperacao()));
        }
        if ((entidade.getSafra() == null) == (entidade.getLote() == null))
            throw erro("Informe safra ou lote.");
        validarDestino(entidade.getSafra(), entidade.getLote(), entidade.getTarefa());
        if (sanitaria
                && (entidade.getLote() == null
                        || entidade.getDosagem() == null
                        || entidade.getDosagem().isBlank()))
            throw erro("Aplicação sanitária exige animal/lote e dosagem registrada.");
        if (categoria.equals("RACAO") && entidade.getLote() == null)
            throw erro("Ração exige lote.");
        if ((categoria.equals("INSUMO") || categoria.equals("DEFENSIVO"))
                && entidade.getSafra() == null) throw erro("Insumo agrícola exige safra.");
        if (categoria.equals("DEFENSIVO")
                && (entidade.getOrientacaoTecnica() == null
                        || entidade.getOrientacaoTecnica().isBlank()))
            throw erro("Defensivo exige orientação técnica registrada.");
        if (entidade.getSafra() != null
                && entidade.getDtaOperacao().isBefore(entidade.getSafra().getDtaInicio()))
            throw erro("Data anterior à safra.");
        if (entidade.getLote() != null
                && entidade.getDtaOperacao().isBefore(entidade.getLote().getDtaFormacao()))
            throw erro("Data anterior ao lote.");
        if (entidade.getManejo() != null
                && (entidade.getSafra() == null
                        || !entidade.getManejo()
                                .getSafra()
                                .getSafraId()
                                .equals(entidade.getSafra().getSafraId())))
            throw erro("Manejo incompatível.");
        if (entidade.getAgendamento() != null) {
            AgendamentoSanitario agendamento =
                    agendamentoSanitarioRepository
                            .bloquear(entidade.getAgendamento().getAgendamentoSanitarioId())
                            .orElseThrow();
            if (agendamento.isConcluido())
                throw erro("Agendamento já concluído; consulte a aplicação existente.");
            if (!sanitaria
                    || !agendamento
                            .getCalendario()
                            .getProduto()
                            .getItemEstoqueId()
                            .equals(entidade.getPartida().getItem().getItemEstoqueId())
                    || (agendamento.getAnimal() != null
                            && (entidade.getAnimal() == null
                                    || !agendamento
                                            .getAnimal()
                                            .getAnimalId()
                                            .equals(entidade.getAnimal().getAnimalId())))
                    || (agendamento.getLote() != null
                            && !agendamento
                                    .getLote()
                                    .getLoteId()
                                    .equals(entidade.getLote().getLoteId())))
                throw erro("Agendamento incompatível.");
            agendamento.setConcluido(true);
        }
        var movimento = new MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequest();
        movimento.setCodPartida(entidade.getPartida().getPartidaEstoqueId());
        movimento.setTipo("USO");
        movimento.setQuantidade(entidade.getQuantidade());
        movimento.setDtaOperacao(entidade.getDtaOperacao());
        movimento.setUnidade(entidade.getUnidade());
        movimento.setMotivo(sanitaria ? "Aplicação sanitária" : "Aplicação agrícola/ração");
        movimento.setCodSafra(
                entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        movimento.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        movimento.setCodTarefa(
                entidade.getTarefa() == null ? null : entidade.getTarefa().getTarefaId());
        movimento.setCodResponsavel(entidade.getResponsavel().getFuncionarioId());
        movimento.setReferenciaOrigem("APLICACAO:" + entidade.getReferencia());
        var consumo = movimentacaoEstoqueService.consumirAplicacao(movimento);
        entidade.setCusto(consumo.getValorTotalOrigem());
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
