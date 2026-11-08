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
public class AbastecimentoService {
    private final AbastecimentoRepository abastecimentoRepository;
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LancamentoRepository lancamentoRepository;
    private final LancamentoService lancamentoService;
    private final LoteRepository loteRepository;
    private final MaquinaRepository maquinaRepository;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<AbastecimentoDTO.AbastecimentoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return abastecimentoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public AbastecimentoDTO.AbastecimentoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public AbastecimentoDTO.AbastecimentoResponse cadastrar(
            AbastecimentoDTO.AbastecimentoRequest request) {
        contexto.bloquearFazenda();

        Abastecimento entidade = new Abastecimento();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = abastecimentoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Abastecimento", entidade.getAbastecimentoId());
        return converterParaResponse(entidade);
    }

    private Abastecimento buscarEntidadePorId(Long id) {
        Abastecimento entidade =
                abastecimentoRepository
                        .findByAbastecimentoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Abastecimento não encontrado nesta fazenda."));

        return entidade;
    }

    private AbastecimentoDTO.AbastecimentoResponse converterParaResponse(Abastecimento entidade) {
        AbastecimentoDTO.AbastecimentoResponse response =
                new AbastecimentoDTO.AbastecimentoResponse();
        response.setAbastecimentoId(entidade.getAbastecimentoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodMaquina(
                entidade.getMaquina() == null ? null : entidade.getMaquina().getMaquinaId());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setLitros(entidade.getLitros());
        response.setCusto(entidade.getCusto());
        response.setLeitura(entidade.getLeitura());
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
            Abastecimento entidade, AbastecimentoDTO.AbastecimentoRequest request) {
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
        entidade.setLitros(request.getLitros());
        entidade.setCusto(request.getCusto());
        entidade.setLeitura(request.getLeitura());
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
        if (!maquina.isAbastecivel()) throw erro("Máquina não abastecível.");
        LocalDate ultimaData =
                abastecimentoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .filter(a -> a.getMaquina().getMaquinaId().equals(maquina.getMaquinaId()))
                        .map(Abastecimento::getDtaOperacao)
                        .max(LocalDate::compareTo)
                        .orElse(null);
        if (ultimaData != null && entidade.getDtaOperacao().isBefore(ultimaData))
            throw erro("Data anterior ao último abastecimento.");
        if (maquina.getUnidadeMedidor() != null) {
            if (entidade.getLeitura() == null
                    || entidade.getLeitura().compareTo(maquina.getLeitura()) < 0
                    || (maquina.getDtaLeitura() != null
                            && entidade.getDtaOperacao().isBefore(maquina.getDtaLeitura())))
                throw erro("Leitura ou data incompatível.");
            maquina.setLeitura(entidade.getLeitura());
            maquina.setDtaLeitura(entidade.getDtaOperacao());
        } else if (entidade.getLeitura() != null) throw erro("Máquina sem medidor.");
        if (entidade.getCusto().signum() > 0)
            entidade.setLancamento(
                    lancamentoService.origemMaquina(
                            entidade.getLancamento(),
                            entidade.getReferencia(),
                            "ABASTECIMENTO",
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
