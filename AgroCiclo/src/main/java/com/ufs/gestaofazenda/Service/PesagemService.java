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
public class PesagemService {
    private final AnimalRepository animalRepository;
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final PesagemRepository pesagemRepository;
    private final RebanhoService rebanhoService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<PesagemDTO.PesagemResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return pesagemRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public PesagemDTO.PesagemResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public PesagemDTO.PesagemResponse cadastrar(PesagemDTO.PesagemRequest request) {
        contexto.bloquearFazenda();

        Pesagem entidade = new Pesagem();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = pesagemRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Pesagem", entidade.getPesagemId());
        return converterParaResponse(entidade);
    }

    private Pesagem buscarEntidadePorId(Long id) {
        Pesagem entidade =
                pesagemRepository
                        .findByPesagemIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Pesagem não encontrado nesta fazenda."));

        return entidade;
    }

    private PesagemDTO.PesagemResponse converterParaResponse(Pesagem entidade) {
        PesagemDTO.PesagemResponse response = new PesagemDTO.PesagemResponse();
        response.setPesagemId(entidade.getPesagemId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodAnimal(
                entidade.getAnimal() == null ? null : entidade.getAnimal().getAnimalId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setDtaPesagem(entidade.getDtaPesagem());
        response.setPeso(entidade.getPeso());
        response.setComposicao(entidade.getComposicao());
        response.setObservacao(entidade.getObservacao());
        return response;
    }

    private void aplicarRequestNaEntidade(Pesagem entidade, PesagemDTO.PesagemRequest request) {
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
        entidade.setDtaPesagem(request.getDtaPesagem());
        entidade.setPeso(request.getPeso());
        entidade.setObservacao(request.getObservacao());
        if ((entidade.getAnimal() == null) == (entidade.getLote() == null))
            throw erro("Informe somente animal ou lote.");
        if (entidade.getAnimal() != null) {
            entidade.setLote(
                    rebanhoService.loteNaData(entidade.getAnimal(), entidade.getDtaPesagem()));
            entidade.setComposicao(entidade.getAnimal().getAnimalId().toString());
        } else {
            var membros = rebanhoService.composicao(entidade.getLote(), entidade.getDtaPesagem());
            if (membros.isEmpty()) throw erro("Lote sem animais na data.");
            entidade.setComposicao(
                    membros.stream()
                            .map(Object::toString)
                            .collect(java.util.stream.Collectors.joining(",")));
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<PesagemDTO.PesagemResponse> evolucao(
            Long codAnimal, Long codLote, LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio) || (codAnimal == null) == (codLote == null))
            throw erro("Informe período e somente animal ou lote.");
        if (codAnimal != null
                && animalRepository
                        .findByAnimalIdAndFazendaFazendaId(codAnimal, contexto.codFazenda())
                        .isEmpty()) throw erro("Animal não autorizado.");
        if (codLote != null
                && loteRepository
                        .findByLoteIdAndFazendaFazendaId(codLote, contexto.codFazenda())
                        .isEmpty()) throw erro("Lote não autorizado.");
        return pesagemRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .filter(p -> !p.getDtaPesagem().isBefore(inicio) && !p.getDtaPesagem().isAfter(fim))
                .filter(
                        p ->
                                codAnimal != null
                                        ? p.getAnimal() != null
                                                && p.getAnimal().getAnimalId().equals(codAnimal)
                                        : p.getAnimal() == null
                                                && p.getLote() != null
                                                && p.getLote().getLoteId().equals(codLote))
                .sorted(Comparator.comparing(Pesagem::getDtaPesagem))
                .map(this::converterParaResponse)
                .toList();
    }
}
