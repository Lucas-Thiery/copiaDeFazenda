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
public class PlanoManejoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final PlanoManejoRepository planoManejoRepository;
    private final SafraRepository safraRepository;
    private final VersaoPlanejamentoRepository versaoPlanejamentoRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<PlanoManejoDTO.PlanoManejoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return planoManejoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PlanoManejoDTO.PlanoManejoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PlanoManejoDTO.PlanoManejoResponse cadastrar(PlanoManejoDTO.PlanoManejoRequest request) {
        contexto.bloquearFazenda();

        PlanoManejo entidade = new PlanoManejo();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = planoManejoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "PlanoManejo", entidade.getPlanoManejoId());
        return converterParaResponse(entidade);
    }

    private PlanoManejo buscarEntidadePorId(Long id) {
        PlanoManejo entidade =
                planoManejoRepository
                        .findByPlanoManejoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "PlanoManejo não encontrado nesta fazenda."));

        return entidade;
    }

    private PlanoManejoDTO.PlanoManejoResponse converterParaResponse(PlanoManejo entidade) {
        PlanoManejoDTO.PlanoManejoResponse response = new PlanoManejoDTO.PlanoManejoResponse();
        response.setPlanoManejoId(entidade.getPlanoManejoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setOperacoes(entidade.getOperacoes());
        response.setDtaInicio(entidade.getDtaInicio());
        response.setDtaFim(entidade.getDtaFim());
        response.setVersao(entidade.getVersao());
        return response;
    }

    private void aplicarRequestNaEntidade(
            PlanoManejo entidade, PlanoManejoDTO.PlanoManejoRequest request) {
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
        entidade.setOperacoes(request.getOperacoes().trim());
        entidade.setDtaInicio(request.getDtaInicio());
        entidade.setDtaFim(request.getDtaFim());
        if (entidade.getDtaFim().isBefore(entidade.getDtaInicio())) throw erro("Período inválido.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PlanoManejoDTO.PlanoManejoResponse atualizar(
            Long id, PlanoManejoDTO.PlanoManejoRequest request) {
        contexto.bloquearFazenda();
        PlanoManejo plano = buscarEntidadePorId(id);
        if (!plano.getSafra().getSafraId().equals(request.getCodSafra()))
            throw erro("A revisão não pode trocar a safra de origem.");
        VersaoPlanejamento versao = new VersaoPlanejamento();
        versao.setFazenda(plano.getFazenda());
        versao.setPlano(plano);
        versao.setOperacoes(plano.getOperacoes());
        versao.setDtaInicio(plano.getDtaInicio());
        versao.setDtaFim(plano.getDtaFim());
        versao.setVersao(plano.getVersao());
        versaoPlanejamentoRepository.save(versao);
        aplicarRequestNaEntidade(plano, request);
        plano.setVersao(plano.getVersao() + 1);
        auditoriaService.registrar("ALTERACAO", "PlanoManejo", id);
        return converterParaResponse(plano);
    }
}
