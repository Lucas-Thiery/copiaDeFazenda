package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.*;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

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
public class FazendaService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FazendaRepository fazendaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<FazendaDTO.FazendaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return contexto.usuario().getFazendas().stream().toList().stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public FazendaDTO.FazendaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public FazendaDTO.FazendaResponse cadastrar(FazendaDTO.FazendaRequest request) {
        contexto.bloquearFazenda();

        Fazenda entidade = new Fazenda();

        aplicarRequestNaEntidade(entidade, request);
        entidade = fazendaRepository.saveAndFlush(entidade);
        Usuario usuario = contexto.usuario();
        usuario.getFazendas().add(entidade);
        auditoriaService.registrar("INCLUSAO", "Fazenda", entidade.getFazendaId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public FazendaDTO.FazendaResponse atualizar(Long id, FazendaDTO.FazendaRequest request) {
        contexto.bloquearFazenda();
        Fazenda entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = fazendaRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Fazenda", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Fazenda entidade = buscarEntidadePorId(id);
        fazendaRepository.delete(entidade);
        fazendaRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Fazenda", id);
    }

    private Fazenda buscarEntidadePorId(Long id) {
        Fazenda entidade =
                fazendaRepository
                        .findById(id)
                        .filter(
                                f ->
                                        contexto.usuario().getFazendas().stream()
                                                .anyMatch(v -> v.getFazendaId().equals(id)))
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Fazenda não encontrado nesta fazenda."));

        return entidade;
    }

    private FazendaDTO.FazendaResponse converterParaResponse(Fazenda entidade) {
        FazendaDTO.FazendaResponse response = new FazendaDTO.FazendaResponse();
        response.setFazendaId(entidade.getFazendaId());
        response.setNome(entidade.getNome());
        response.setAreaHectares(entidade.getAreaHectares());
        response.setLocalizacao(entidade.getLocalizacao());
        response.setResponsavel(entidade.getResponsavel());
        response.setAtiva(entidade.isAtiva());
        response.setDtaCadastro(entidade.getDtaCadastro());
        return response;
    }

    private void aplicarRequestNaEntidade(Fazenda entidade, FazendaDTO.FazendaRequest request) {
        entidade.setNome(request.getNome().trim());
        entidade.setAreaHectares(request.getAreaHectares());
        entidade.setLocalizacao(request.getLocalizacao().trim());
        entidade.setResponsavel(request.getResponsavel().trim());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
