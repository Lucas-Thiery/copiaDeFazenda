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
public class CalendarioSanitarioService {
    private final AuditoriaService auditoriaService;
    private final CalendarioSanitarioRepository calendarioSanitarioRepository;
    private final ContextoUsuario contexto;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<CalendarioSanitarioDTO.CalendarioSanitarioResponse> listar(
            int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return calendarioSanitarioRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public CalendarioSanitarioDTO.CalendarioSanitarioResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public CalendarioSanitarioDTO.CalendarioSanitarioResponse cadastrar(
            CalendarioSanitarioDTO.CalendarioSanitarioRequest request) {
        contexto.bloquearFazenda();

        CalendarioSanitario entidade = new CalendarioSanitario();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = calendarioSanitarioRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "CalendarioSanitario", entidade.getCalendarioSanitarioId());
        return converterParaResponse(entidade);
    }

    private CalendarioSanitario buscarEntidadePorId(Long id) {
        CalendarioSanitario entidade =
                calendarioSanitarioRepository
                        .findByCalendarioSanitarioIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "CalendarioSanitario não encontrado nesta"
                                                    + " fazenda."));

        return entidade;
    }

    private CalendarioSanitarioDTO.CalendarioSanitarioResponse converterParaResponse(
            CalendarioSanitario entidade) {
        CalendarioSanitarioDTO.CalendarioSanitarioResponse response =
                new CalendarioSanitarioDTO.CalendarioSanitarioResponse();
        response.setCalendarioSanitarioId(entidade.getCalendarioSanitarioId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setEspecie(entidade.getEspecie());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodProduto(
                entidade.getProduto() == null ? null : entidade.getProduto().getItemEstoqueId());
        response.setAcao(entidade.getAcao());
        response.setOrientacaoTecnica(entidade.getOrientacaoTecnica());
        response.setDtaPrevista(entidade.getDtaPrevista());
        response.setIntervaloDias(entidade.getIntervaloDias());
        return response;
    }

    private void aplicarRequestNaEntidade(
            CalendarioSanitario entidade,
            CalendarioSanitarioDTO.CalendarioSanitarioRequest request) {
        entidade.setEspecie(request.getEspecie());
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
        entidade.setProduto(
                request.getCodProduto() == null
                        ? null
                        : itemEstoqueRepository
                                .findById(request.getCodProduto())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "ItemEstoque inexistente ou de outra"
                                                            + " fazenda.")));
        entidade.setAcao(request.getAcao().trim());
        entidade.setOrientacaoTecnica(request.getOrientacaoTecnica().trim());
        entidade.setDtaPrevista(request.getDtaPrevista());
        entidade.setIntervaloDias(request.getIntervaloDias());
        if ((entidade.getEspecie() == null || entidade.getEspecie().isBlank())
                && entidade.getLote() == null) throw erro("Informe espécie ou lote.");
        if (entidade.getDtaPrevista() == null && entidade.getIntervaloDias() == null)
            throw erro("Informe data ou intervalo técnico.");
        if (!Set.of("VACINA", "MEDICAMENTO").contains(entidade.getProduto().getCategoria()))
            throw erro("Produto não sanitário.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
