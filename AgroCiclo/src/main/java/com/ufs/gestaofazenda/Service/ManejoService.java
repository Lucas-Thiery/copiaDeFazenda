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
public class ManejoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FuncionarioRepository funcionarioRepository;
    private final ManejoRepository manejoRepository;
    private final SafraRepository safraRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<ManejoDTO.ManejoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return manejoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ManejoDTO.ManejoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ManejoDTO.ManejoResponse cadastrar(ManejoDTO.ManejoRequest request) {
        contexto.bloquearFazenda();

        Manejo entidade = new Manejo();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = manejoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Manejo", entidade.getManejoId());
        return converterParaResponse(entidade);
    }

    private Manejo buscarEntidadePorId(Long id) {
        Manejo entidade =
                manejoRepository
                        .findByManejoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Manejo não encontrado nesta fazenda."));

        return entidade;
    }

    private ManejoDTO.ManejoResponse converterParaResponse(Manejo entidade) {
        ManejoDTO.ManejoResponse response = new ManejoDTO.ManejoResponse();
        response.setManejoId(entidade.getManejoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setOperacao(entidade.getOperacao());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setCodResponsavel(
                entidade.getResponsavel() == null
                        ? null
                        : entidade.getResponsavel().getFuncionarioId());
        return response;
    }

    private void aplicarRequestNaEntidade(Manejo entidade, ManejoDTO.ManejoRequest request) {
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
        entidade.setOperacao(request.getOperacao().trim());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setResponsavel(
                request.getCodResponsavel() == null
                        ? null
                        : funcionarioRepository
                                .findById(request.getCodResponsavel())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "Funcionario inexistente ou de outra"
                                                            + " fazenda.")));
        if (entidade.getSafra().getStatus().equals("ENCERRADA")
                || entidade.getDtaOperacao().isBefore(entidade.getSafra().getDtaInicio()))
            throw erro("Manejo fora do ciclo da safra.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
