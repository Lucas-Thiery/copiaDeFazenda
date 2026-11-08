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
public class ProducaoColheitaService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final EtapaCultivoRepository etapaCultivoRepository;
    private final ProducaoColheitaRepository producaoColheitaRepository;
    private final SafraRepository safraRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<ProducaoColheitaDTO.ProducaoColheitaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return producaoColheitaRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ProducaoColheitaDTO.ProducaoColheitaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ProducaoColheitaDTO.ProducaoColheitaResponse cadastrar(
            ProducaoColheitaDTO.ProducaoColheitaRequest request) {
        contexto.bloquearFazenda();

        ProducaoColheita entidade = new ProducaoColheita();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = producaoColheitaRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "ProducaoColheita", entidade.getProducaoColheitaId());
        return converterParaResponse(entidade);
    }

    private ProducaoColheita buscarEntidadePorId(Long id) {
        ProducaoColheita entidade =
                producaoColheitaRepository
                        .findByProducaoColheitaIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "ProducaoColheita não encontrado nesta fazenda."));

        return entidade;
    }

    private ProducaoColheitaDTO.ProducaoColheitaResponse converterParaResponse(
            ProducaoColheita entidade) {
        ProducaoColheitaDTO.ProducaoColheitaResponse response =
                new ProducaoColheitaDTO.ProducaoColheitaResponse();
        response.setProducaoColheitaId(entidade.getProducaoColheitaId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setQuantidade(entidade.getQuantidade());
        response.setUnidade(entidade.getUnidade());
        response.setJustificativaPerda(entidade.getJustificativaPerda());
        response.setParcial(entidade.isParcial());
        return response;
    }

    private void aplicarRequestNaEntidade(
            ProducaoColheita entidade, ProducaoColheitaDTO.ProducaoColheitaRequest request) {
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
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setQuantidade(request.getQuantidade());
        entidade.setUnidade(request.getUnidade().trim());
        entidade.setJustificativaPerda(request.getJustificativaPerda());
        entidade.setParcial(request.isParcial());
        Safra safra = safraRepository.bloquear(entidade.getSafra().getSafraId()).orElseThrow();
        if (!safra.getStatus().equals("COLHEITA"))
            throw erro("Colheita não iniciada ou encerrada.");
        LocalDate inicio =
                etapaCultivoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .filter(
                                e ->
                                        e.getSafra().getSafraId().equals(safra.getSafraId())
                                                && e.getEtapa().equals("COLHEITA"))
                        .map(EtapaCultivo::getDtaOperacao)
                        .findFirst()
                        .orElseThrow();
        if (entidade.getDtaOperacao().isBefore(inicio))
            throw erro("Data anterior ao início da colheita.");
        var anteriores =
                producaoColheitaRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .filter(p -> p.getSafra().getSafraId().equals(safra.getSafraId()))
                        .toList();
        if (anteriores.stream().anyMatch(p -> p.getQuantidade().signum() == 0))
            throw erro("Perda total já registrada.");
        if (anteriores.stream().anyMatch(p -> !p.getUnidade().equals(entidade.getUnidade())))
            throw erro("Unidade incompatível.");
        if (entidade.getQuantidade().signum() == 0
                && (!anteriores.isEmpty()
                        || entidade.getJustificativaPerda() == null
                        || entidade.getJustificativaPerda().isBlank()))
            throw erro("Perda total exige justificativa e ausência de produção positiva.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
