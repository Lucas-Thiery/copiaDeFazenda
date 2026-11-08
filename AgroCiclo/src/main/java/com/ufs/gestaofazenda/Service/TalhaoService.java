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
public class TalhaoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final TalhaoRepository talhaoRepository;
    private final VersaoLimitesRepository versaoLimitesRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<TalhaoDTO.TalhaoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return talhaoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public TalhaoDTO.TalhaoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public TalhaoDTO.TalhaoResponse cadastrar(TalhaoDTO.TalhaoRequest request) {
        contexto.bloquearFazenda();
        if (talhaoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Talhao já cadastrado.");
        Talhao entidade = new Talhao();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = talhaoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Talhao", entidade.getTalhaoId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public TalhaoDTO.TalhaoResponse atualizar(Long id, TalhaoDTO.TalhaoRequest request) {
        contexto.bloquearFazenda();
        Talhao entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = talhaoRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Talhao", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Talhao entidade = buscarEntidadePorId(id);
        talhaoRepository.delete(entidade);
        talhaoRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Talhao", id);
    }

    private Talhao buscarEntidadePorId(Long id) {
        Talhao entidade =
                talhaoRepository
                        .findByTalhaoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Talhao não encontrado nesta fazenda."));

        return entidade;
    }

    private TalhaoDTO.TalhaoResponse converterParaResponse(Talhao entidade) {
        TalhaoDTO.TalhaoResponse response = new TalhaoDTO.TalhaoResponse();
        response.setTalhaoId(entidade.getTalhaoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setAreaHectares(entidade.getAreaHectares());
        response.setLocalizacao(entidade.getLocalizacao());
        response.setDescricaoLimites(entidade.getDescricaoLimites());
        response.setDtaCadastro(entidade.getDtaCadastro());
        return response;
    }

    private void aplicarRequestNaEntidade(Talhao entidade, TalhaoDTO.TalhaoRequest request) {
        String anteriorLimites = entidade.getDescricaoLimites();
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setAreaHectares(request.getAreaHectares());
        entidade.setLocalizacao(request.getLocalizacao());
        entidade.setDescricaoLimites(request.getDescricaoLimites());
        if (entidade.getAreaHectares().compareTo(contexto.fazenda().getAreaHectares()) > 0)
            throw erro("Área do talhão não pode exceder a área da fazenda.");
        if (entidade.getTalhaoId() != null) {
            VersaoLimites versao = new VersaoLimites();
            versao.setFazenda(entidade.getFazenda());
            versao.setTalhao(entidade);
            versao.setDescricaoLimites(anteriorLimites);
            versaoLimitesRepository.save(versao);
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
