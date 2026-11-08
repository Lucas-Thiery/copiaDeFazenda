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
public class MovimentacaoPastagemService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final MovimentacaoPastagemRepository movimentacaoPastagemRepository;
    private final PastagemRepository pastagemRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<MovimentacaoPastagemDTO.MovimentacaoPastagemResponse> listar(
            int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return movimentacaoPastagemRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public MovimentacaoPastagemDTO.MovimentacaoPastagemResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public MovimentacaoPastagemDTO.MovimentacaoPastagemResponse cadastrar(
            MovimentacaoPastagemDTO.MovimentacaoPastagemRequest request) {
        contexto.bloquearFazenda();

        MovimentacaoPastagem entidade = new MovimentacaoPastagem();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = movimentacaoPastagemRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "MovimentacaoPastagem", entidade.getMovimentacaoPastagemId());
        return converterParaResponse(entidade);
    }

    private MovimentacaoPastagem buscarEntidadePorId(Long id) {
        MovimentacaoPastagem entidade =
                movimentacaoPastagemRepository
                        .findByMovimentacaoPastagemIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "MovimentacaoPastagem não encontrado nesta"
                                                    + " fazenda."));

        return entidade;
    }

    private MovimentacaoPastagemDTO.MovimentacaoPastagemResponse converterParaResponse(
            MovimentacaoPastagem entidade) {
        MovimentacaoPastagemDTO.MovimentacaoPastagemResponse response =
                new MovimentacaoPastagemDTO.MovimentacaoPastagemResponse();
        response.setMovimentacaoPastagemId(entidade.getMovimentacaoPastagemId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodOrigem(
                entidade.getOrigem() == null ? null : entidade.getOrigem().getPastagemId());
        response.setCodDestino(
                entidade.getDestino() == null ? null : entidade.getDestino().getPastagemId());
        response.setDtaOperacao(entidade.getDtaOperacao());
        return response;
    }

    private void aplicarRequestNaEntidade(
            MovimentacaoPastagem entidade,
            MovimentacaoPastagemDTO.MovimentacaoPastagemRequest request) {
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
        entidade.setOrigem(
                request.getCodOrigem() == null
                        ? null
                        : pastagemRepository
                                .findById(request.getCodOrigem())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Pastagem inexistente ou de outra fazenda.")));
        entidade.setDestino(
                request.getCodDestino() == null
                        ? null
                        : pastagemRepository
                                .findById(request.getCodDestino())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Pastagem inexistente ou de outra fazenda.")));
        entidade.setDtaOperacao(request.getDtaOperacao());
        Lote lote = loteRepository.bloquear(entidade.getLote().getLoteId()).orElseThrow();
        if (entidade.getOrigem() != null) throw erro("Origem é determinada pelo sistema.");
        var ultima =
                movimentacaoPastagemRepository
                        .findAllByFazendaFazendaId(contexto.codFazenda())
                        .stream()
                        .filter(m -> m.getLote().getLoteId().equals(lote.getLoteId()))
                        .map(MovimentacaoPastagem::getDtaOperacao)
                        .max(LocalDate::compareTo)
                        .orElse(lote.getDtaFormacao());
        if (entidade.getDtaOperacao().isBefore(ultima)
                || entidade.getDestino()
                        .getPastagemId()
                        .equals(lote.getPastagemAtual().getPastagemId()))
            throw erro("Data inválida ou destino igual à origem.");
        entidade.setOrigem(lote.getPastagemAtual());
        lote.setPastagemAtual(entidade.getDestino());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
