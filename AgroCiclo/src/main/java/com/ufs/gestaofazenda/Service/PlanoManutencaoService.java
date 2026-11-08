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
public class PlanoManutencaoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final MaquinaRepository maquinaRepository;
    private final PlanoManutencaoRepository planoManutencaoRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<PlanoManutencaoDTO.PlanoManutencaoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return planoManutencaoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PlanoManutencaoDTO.PlanoManutencaoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PlanoManutencaoDTO.PlanoManutencaoResponse cadastrar(
            PlanoManutencaoDTO.PlanoManutencaoRequest request) {
        contexto.bloquearFazenda();

        PlanoManutencao entidade = new PlanoManutencao();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = planoManutencaoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "PlanoManutencao", entidade.getPlanoManutencaoId());
        return converterParaResponse(entidade);
    }

    private PlanoManutencao buscarEntidadePorId(Long id) {
        PlanoManutencao entidade =
                planoManutencaoRepository
                        .findByPlanoManutencaoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "PlanoManutencao não encontrado nesta fazenda."));

        return entidade;
    }

    private PlanoManutencaoDTO.PlanoManutencaoResponse converterParaResponse(
            PlanoManutencao entidade) {
        PlanoManutencaoDTO.PlanoManutencaoResponse response =
                new PlanoManutencaoDTO.PlanoManutencaoResponse();
        response.setPlanoManutencaoId(entidade.getPlanoManutencaoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodMaquina(
                entidade.getMaquina() == null ? null : entidade.getMaquina().getMaquinaId());
        response.setServico(entidade.getServico());
        response.setDtaPrevista(entidade.getDtaPrevista());
        response.setLimiteMedidor(entidade.getLimiteMedidor());
        response.setConcluido(entidade.isConcluido());
        return response;
    }

    private void aplicarRequestNaEntidade(
            PlanoManutencao entidade, PlanoManutencaoDTO.PlanoManutencaoRequest request) {
        entidade.setMaquina(
                request.getCodMaquina() == null
                        ? null
                        : maquinaRepository
                                .findById(request.getCodMaquina())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Maquina inexistente ou de outra fazenda.")));
        entidade.setServico(request.getServico().trim());
        entidade.setDtaPrevista(request.getDtaPrevista());
        entidade.setLimiteMedidor(request.getLimiteMedidor());
        if (entidade.getDtaPrevista() == null && entidade.getLimiteMedidor() == null)
            throw erro("Informe data ou limite de uso.");
        if (entidade.getLimiteMedidor() != null
                && entidade.getMaquina().getUnidadeMedidor() == null)
            throw erro("Máquina sem medidor exige data.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
