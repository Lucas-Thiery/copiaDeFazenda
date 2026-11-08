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
public class VersaoLimitesService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final TalhaoRepository talhaoRepository;
    private final VersaoLimitesRepository versaoLimitesRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<VersaoLimitesDTO.VersaoLimitesResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return versaoLimitesRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public VersaoLimitesDTO.VersaoLimitesResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    private VersaoLimites buscarEntidadePorId(Long id) {
        VersaoLimites entidade =
                versaoLimitesRepository
                        .findByVersaoLimitesIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "VersaoLimites não encontrado nesta fazenda."));

        return entidade;
    }

    private VersaoLimitesDTO.VersaoLimitesResponse converterParaResponse(VersaoLimites entidade) {
        VersaoLimitesDTO.VersaoLimitesResponse response =
                new VersaoLimitesDTO.VersaoLimitesResponse();
        response.setVersaoLimitesId(entidade.getVersaoLimitesId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodTalhao(
                entidade.getTalhao() == null ? null : entidade.getTalhao().getTalhaoId());
        response.setDescricaoLimites(entidade.getDescricaoLimites());
        response.setDtaRegistro(entidade.getDtaRegistro());
        return response;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
