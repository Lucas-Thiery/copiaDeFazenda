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
public class FornecedorService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<FornecedorDTO.FornecedorResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return fornecedorRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public FornecedorDTO.FornecedorResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public FornecedorDTO.FornecedorResponse cadastrar(FornecedorDTO.FornecedorRequest request) {
        contexto.bloquearFazenda();
        if (fornecedorRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Fornecedor já cadastrado.");
        Fornecedor entidade = new Fornecedor();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = fornecedorRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Fornecedor", entidade.getFornecedorId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public FornecedorDTO.FornecedorResponse atualizar(
            Long id, FornecedorDTO.FornecedorRequest request) {
        contexto.bloquearFazenda();
        Fornecedor entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = fornecedorRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Fornecedor", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Fornecedor entidade = buscarEntidadePorId(id);
        fornecedorRepository.delete(entidade);
        fornecedorRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Fornecedor", id);
    }

    private Fornecedor buscarEntidadePorId(Long id) {
        Fornecedor entidade =
                fornecedorRepository
                        .findByFornecedorIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Fornecedor não encontrado nesta fazenda."));

        return entidade;
    }

    private FornecedorDTO.FornecedorResponse converterParaResponse(Fornecedor entidade) {
        FornecedorDTO.FornecedorResponse response = new FornecedorDTO.FornecedorResponse();
        response.setFornecedorId(entidade.getFornecedorId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setContato(entidade.getContato());
        return response;
    }

    private void aplicarRequestNaEntidade(
            Fornecedor entidade, FornecedorDTO.FornecedorRequest request) {
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setContato(request.getContato().trim());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
