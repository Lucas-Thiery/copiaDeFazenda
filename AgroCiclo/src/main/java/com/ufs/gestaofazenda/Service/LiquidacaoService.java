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
public class LiquidacaoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LancamentoRepository lancamentoRepository;
    private final LiquidacaoRepository liquidacaoRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public List<LiquidacaoDTO.LiquidacaoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return liquidacaoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public LiquidacaoDTO.LiquidacaoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public LiquidacaoDTO.LiquidacaoResponse cadastrar(LiquidacaoDTO.LiquidacaoRequest request) {
        contexto.bloquearFazenda();

        Liquidacao entidade = new Liquidacao();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = liquidacaoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Liquidacao", entidade.getLiquidacaoId());
        return converterParaResponse(entidade);
    }

    private Liquidacao buscarEntidadePorId(Long id) {
        Liquidacao entidade =
                liquidacaoRepository
                        .findByLiquidacaoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Liquidacao não encontrado nesta fazenda."));

        return entidade;
    }

    private LiquidacaoDTO.LiquidacaoResponse converterParaResponse(Liquidacao entidade) {
        LiquidacaoDTO.LiquidacaoResponse response = new LiquidacaoDTO.LiquidacaoResponse();
        response.setLiquidacaoId(entidade.getLiquidacaoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodLancamento(
                entidade.getLancamento() == null
                        ? null
                        : entidade.getLancamento().getLancamentoId());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setValor(entidade.getValor());
        response.setComprovante(entidade.getComprovante());
        return response;
    }

    private void aplicarRequestNaEntidade(
            Liquidacao entidade, LiquidacaoDTO.LiquidacaoRequest request) {
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
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setValor(request.getValor());
        entidade.setComprovante(request.getComprovante().trim());
        Lancamento lancamento =
                lancamentoRepository
                        .bloquear(entidade.getLancamento().getLancamentoId())
                        .orElseThrow();
        if (entidade.getDtaOperacao().isBefore(lancamento.getDtaOperacao()))
            throw erro("Liquidação anterior à origem.");
        if (entidade.getValor()
                        .compareTo(lancamento.getValor().subtract(lancamento.getValorLiquidado()))
                > 0) throw erro("Valor superior ao saldo pendente.");
        if (liquidacaoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(l -> l.getComprovante().equals(entidade.getComprovante())))
            throw erro("Comprovante já utilizado.");
        lancamento.setValorLiquidado(lancamento.getValorLiquidado().add(entidade.getValor()));
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
