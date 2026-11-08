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
public class SafraService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final SafraRepository safraRepository;
    private final TalhaoRepository talhaoRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<SafraDTO.SafraResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return safraRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public SafraDTO.SafraResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public SafraDTO.SafraResponse cadastrar(SafraDTO.SafraRequest request) {
        contexto.bloquearFazenda();
        if (safraRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Safra já cadastrado.");
        Safra entidade = new Safra();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = safraRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Safra", entidade.getSafraId());
        return converterParaResponse(entidade);
    }

    private Safra buscarEntidadePorId(Long id) {
        Safra entidade =
                safraRepository
                        .findBySafraIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Safra não encontrado nesta fazenda."));

        return entidade;
    }

    private SafraDTO.SafraResponse converterParaResponse(Safra entidade) {
        SafraDTO.SafraResponse response = new SafraDTO.SafraResponse();
        response.setSafraId(entidade.getSafraId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodTalhao(
                entidade.getTalhao() == null ? null : entidade.getTalhao().getTalhaoId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setCultura(entidade.getCultura());
        response.setVariedade(entidade.getVariedade());
        response.setDtaInicio(entidade.getDtaInicio());
        response.setDtaFimPrevista(entidade.getDtaFimPrevista());
        response.setAreaPlantadaHa(entidade.getAreaPlantadaHa());
        response.setStatus(entidade.getStatus());
        response.setObservacoes(entidade.getObservacoes());
        return response;
    }

    private void aplicarRequestNaEntidade(Safra entidade, SafraDTO.SafraRequest request) {
        entidade.setTalhao(
                request.getCodTalhao() == null
                        ? null
                        : talhaoRepository
                                .findById(request.getCodTalhao())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Talhao inexistente ou de outra fazenda.")));
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setCultura(request.getCultura().trim());
        entidade.setVariedade(request.getVariedade());
        entidade.setDtaInicio(request.getDtaInicio());
        entidade.setDtaFimPrevista(request.getDtaFimPrevista());
        entidade.setAreaPlantadaHa(request.getAreaPlantadaHa());
        entidade.setObservacoes(request.getObservacoes());
        if (entidade.getAreaPlantadaHa().compareTo(entidade.getTalhao().getAreaHectares()) > 0)
            throw erro("Área plantada excede a área do talhão.");
        if (entidade.getDtaFimPrevista() != null
                && entidade.getDtaFimPrevista().isBefore(entidade.getDtaInicio()))
            throw erro("Data final anterior à inicial.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
