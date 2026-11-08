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
public class PastagemService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final PastagemRepository pastagemRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<PastagemDTO.PastagemResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return pastagemRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PastagemDTO.PastagemResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PastagemDTO.PastagemResponse cadastrar(PastagemDTO.PastagemRequest request) {
        contexto.bloquearFazenda();
        if (pastagemRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Pastagem já cadastrado.");
        Pastagem entidade = new Pastagem();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = pastagemRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Pastagem", entidade.getPastagemId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PastagemDTO.PastagemResponse atualizar(Long id, PastagemDTO.PastagemRequest request) {
        contexto.bloquearFazenda();
        Pastagem entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = pastagemRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Pastagem", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Pastagem entidade = buscarEntidadePorId(id);
        pastagemRepository.delete(entidade);
        pastagemRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Pastagem", id);
    }

    private Pastagem buscarEntidadePorId(Long id) {
        Pastagem entidade =
                pastagemRepository
                        .findByPastagemIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Pastagem não encontrado nesta fazenda."));

        return entidade;
    }

    private PastagemDTO.PastagemResponse converterParaResponse(Pastagem entidade) {
        PastagemDTO.PastagemResponse response = new PastagemDTO.PastagemResponse();
        response.setPastagemId(entidade.getPastagemId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setDescricao(entidade.getDescricao());
        response.setAtiva(entidade.isAtiva());
        return response;
    }

    private void aplicarRequestNaEntidade(Pastagem entidade, PastagemDTO.PastagemRequest request) {
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setDescricao(request.getDescricao().trim());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
