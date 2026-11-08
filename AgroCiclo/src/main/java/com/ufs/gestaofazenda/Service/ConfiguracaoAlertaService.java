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
public class ConfiguracaoAlertaService {
    private final AuditoriaService auditoriaService;
    private final ConfiguracaoAlertaRepository configuracaoAlertaRepository;
    private final ContextoUsuario contexto;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return configuracaoAlertaRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse cadastrar(
            ConfiguracaoAlertaDTO.ConfiguracaoAlertaRequest request) {
        contexto.bloquearFazenda();

        ConfiguracaoAlerta entidade = new ConfiguracaoAlerta();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = configuracaoAlertaRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "ConfiguracaoAlerta", entidade.getConfiguracaoAlertaId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse atualizar(
            Long id, ConfiguracaoAlertaDTO.ConfiguracaoAlertaRequest request) {
        contexto.bloquearFazenda();
        ConfiguracaoAlerta entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = configuracaoAlertaRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "ConfiguracaoAlerta", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        ConfiguracaoAlerta entidade = buscarEntidadePorId(id);
        configuracaoAlertaRepository.delete(entidade);
        configuracaoAlertaRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "ConfiguracaoAlerta", id);
    }

    private ConfiguracaoAlerta buscarEntidadePorId(Long id) {
        ConfiguracaoAlerta entidade =
                configuracaoAlertaRepository
                        .findByConfiguracaoAlertaIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "ConfiguracaoAlerta não encontrado nesta"
                                                    + " fazenda."));

        return entidade;
    }

    private ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse converterParaResponse(
            ConfiguracaoAlerta entidade) {
        ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse response =
                new ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse();
        response.setConfiguracaoAlertaId(entidade.getConfiguracaoAlertaId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setDiasVacina(entidade.getDiasVacina());
        response.setDiasValidade(entidade.getDiasValidade());
        response.setDiasManutencao(entidade.getDiasManutencao());
        response.setMargemHoras(entidade.getMargemHoras());
        response.setMargemKm(entidade.getMargemKm());
        return response;
    }

    private void aplicarRequestNaEntidade(
            ConfiguracaoAlerta entidade, ConfiguracaoAlertaDTO.ConfiguracaoAlertaRequest request) {
        entidade.setDiasVacina(request.getDiasVacina());
        entidade.setDiasValidade(request.getDiasValidade());
        entidade.setDiasManutencao(request.getDiasManutencao());
        entidade.setMargemHoras(request.getMargemHoras());
        entidade.setMargemKm(request.getMargemKm());
        if (configuracaoAlertaRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        c ->
                                !Objects.equals(
                                        c.getConfiguracaoAlertaId(),
                                        entidade.getConfiguracaoAlertaId())))
            throw erro("Configuração já cadastrada; utilize atualizar.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
