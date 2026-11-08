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
public class VinculoAnimalLoteService {
    private final AnimalRepository animalRepository;
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final VinculoAnimalLoteRepository vinculoAnimalLoteRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<VinculoAnimalLoteDTO.VinculoAnimalLoteResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return vinculoAnimalLoteRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public VinculoAnimalLoteDTO.VinculoAnimalLoteResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    private VinculoAnimalLote buscarEntidadePorId(Long id) {
        VinculoAnimalLote entidade =
                vinculoAnimalLoteRepository
                        .findByVinculoAnimalLoteIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "VinculoAnimalLote não encontrado nesta fazenda."));

        return entidade;
    }

    private VinculoAnimalLoteDTO.VinculoAnimalLoteResponse converterParaResponse(
            VinculoAnimalLote entidade) {
        VinculoAnimalLoteDTO.VinculoAnimalLoteResponse response =
                new VinculoAnimalLoteDTO.VinculoAnimalLoteResponse();
        response.setVinculoAnimalLoteId(entidade.getVinculoAnimalLoteId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodAnimal(
                entidade.getAnimal() == null ? null : entidade.getAnimal().getAnimalId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setDtaInicio(entidade.getDtaInicio());
        response.setDtaFim(entidade.getDtaFim());
        return response;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
