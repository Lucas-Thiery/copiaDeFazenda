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
public class ProducaoPecuariaService {
    private final AnimalRepository animalRepository;
    private final AuditoriaService auditoriaService;
    private final CarenciaService carenciaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final ProducaoPecuariaRepository producaoPecuariaRepository;
    private final RebanhoService rebanhoService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<ProducaoPecuariaDTO.ProducaoPecuariaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return producaoPecuariaRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public ProducaoPecuariaDTO.ProducaoPecuariaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public ProducaoPecuariaDTO.ProducaoPecuariaResponse cadastrar(
            ProducaoPecuariaDTO.ProducaoPecuariaRequest request) {
        contexto.bloquearFazenda();

        ProducaoPecuaria entidade = new ProducaoPecuaria();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = producaoPecuariaRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "ProducaoPecuaria", entidade.getProducaoPecuariaId());
        return converterParaResponse(entidade);
    }

    private ProducaoPecuaria buscarEntidadePorId(Long id) {
        ProducaoPecuaria entidade =
                producaoPecuariaRepository
                        .findByProducaoPecuariaIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "ProducaoPecuaria não encontrado nesta fazenda."));

        return entidade;
    }

    private ProducaoPecuariaDTO.ProducaoPecuariaResponse converterParaResponse(
            ProducaoPecuaria entidade) {
        ProducaoPecuariaDTO.ProducaoPecuariaResponse response =
                new ProducaoPecuariaDTO.ProducaoPecuariaResponse();
        response.setProducaoPecuariaId(entidade.getProducaoPecuariaId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodAnimal(
                entidade.getAnimal() == null ? null : entidade.getAnimal().getAnimalId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setProduto(entidade.getProduto());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setQuantidade(entidade.getQuantidade());
        response.setUnidade(entidade.getUnidade());
        response.setDestinacao(entidade.getDestinacao());
        response.setAvisoCarencia(entidade.getAvisoCarencia());
        return response;
    }

    private void aplicarRequestNaEntidade(
            ProducaoPecuaria entidade, ProducaoPecuariaDTO.ProducaoPecuariaRequest request) {
        entidade.setAnimal(
                request.getCodAnimal() == null
                        ? null
                        : animalRepository
                                .findById(request.getCodAnimal())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Animal inexistente ou de outra fazenda.")));
        entidade.setLote(
                request.getCodLote() == null
                        ? null
                        : loteRepository
                                .findById(request.getCodLote())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(() -> erro("Lote inexistente ou de outra fazenda.")));
        entidade.setProduto(request.getProduto().trim());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setQuantidade(request.getQuantidade());
        entidade.setUnidade(request.getUnidade().trim());
        entidade.setDestinacao(request.getDestinacao());
        if ((entidade.getAnimal() == null) == (entidade.getLote() == null))
            throw erro("Informe animal ou lote.");
        if (entidade.getAnimal() != null)
            entidade.setLote(
                    rebanhoService.loteNaData(entidade.getAnimal(), entidade.getDtaOperacao()));
        if (entidade.getDtaOperacao().isBefore(entidade.getLote().getDtaFormacao()))
            throw erro("Data anterior ao lote.");
        if (Set.of("CONSUMO", "VENDA")
                .contains(entidade.getDestinacao() == null ? "" : entidade.getDestinacao()))
            entidade.setAvisoCarencia(
                    carenciaService.aviso(null, entidade.getLote(), entidade.getDtaOperacao()));
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
