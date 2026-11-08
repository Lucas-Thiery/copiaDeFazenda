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
public class LoteService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final MovimentacaoPastagemRepository movimentacaoPastagemRepository;
    private final PastagemRepository pastagemRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<LoteDTO.LoteResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return loteRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public LoteDTO.LoteResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public LoteDTO.LoteResponse cadastrar(LoteDTO.LoteRequest request) {
        contexto.bloquearFazenda();
        if (loteRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Lote já cadastrado.");
        Lote entidade = new Lote();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = loteRepository.saveAndFlush(entidade);
        MovimentacaoPastagem movimento = new MovimentacaoPastagem();
        movimento.setFazenda(entidade.getFazenda());
        movimento.setLote(entidade);
        movimento.setDestino(entidade.getPastagemAtual());
        movimento.setDtaOperacao(entidade.getDtaFormacao());
        movimentacaoPastagemRepository.save(movimento);
        auditoriaService.registrar("INCLUSAO", "Lote", entidade.getLoteId());
        return converterParaResponse(entidade);
    }

    private Lote buscarEntidadePorId(Long id) {
        Lote entidade =
                loteRepository
                        .findByLoteIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Lote não encontrado nesta fazenda."));

        return entidade;
    }

    private LoteDTO.LoteResponse converterParaResponse(Lote entidade) {
        LoteDTO.LoteResponse response = new LoteDTO.LoteResponse();
        response.setLoteId(entidade.getLoteId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodPastagemAtual(
                entidade.getPastagemAtual() == null
                        ? null
                        : entidade.getPastagemAtual().getPastagemId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setDtaFormacao(entidade.getDtaFormacao());
        response.setAtivo(entidade.isAtivo());
        return response;
    }

    private void aplicarRequestNaEntidade(Lote entidade, LoteDTO.LoteRequest request) {
        entidade.setPastagemAtual(
                request.getCodPastagemAtual() == null
                        ? null
                        : pastagemRepository
                                .findById(request.getCodPastagemAtual())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Pastagem inexistente ou de outra fazenda.")));
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setDtaFormacao(request.getDtaFormacao());
        if (!entidade.getPastagemAtual().isAtiva()) throw erro("Pastagem inativa.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
