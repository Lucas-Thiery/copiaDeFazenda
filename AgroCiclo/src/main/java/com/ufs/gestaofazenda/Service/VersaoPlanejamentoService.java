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
public class VersaoPlanejamentoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final PlanoManejoRepository planoManejoRepository;
    private final VersaoPlanejamentoRepository versaoPlanejamentoRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<VersaoPlanejamentoDTO.VersaoPlanejamentoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return versaoPlanejamentoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public VersaoPlanejamentoDTO.VersaoPlanejamentoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    private VersaoPlanejamento buscarEntidadePorId(Long id) {
        VersaoPlanejamento entidade =
                versaoPlanejamentoRepository
                        .findByVersaoPlanejamentoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "VersaoPlanejamento não encontrado nesta"
                                                    + " fazenda."));

        return entidade;
    }

    private VersaoPlanejamentoDTO.VersaoPlanejamentoResponse converterParaResponse(
            VersaoPlanejamento entidade) {
        VersaoPlanejamentoDTO.VersaoPlanejamentoResponse response =
                new VersaoPlanejamentoDTO.VersaoPlanejamentoResponse();
        response.setVersaoPlanejamentoId(entidade.getVersaoPlanejamentoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodPlano(
                entidade.getPlano() == null ? null : entidade.getPlano().getPlanoManejoId());
        response.setOperacoes(entidade.getOperacoes());
        response.setDtaInicio(entidade.getDtaInicio());
        response.setDtaFim(entidade.getDtaFim());
        response.setVersao(entidade.getVersao());
        return response;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
