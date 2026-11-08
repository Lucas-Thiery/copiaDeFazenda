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
public class EtapaCultivoService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final EtapaCultivoRepository etapaCultivoRepository;
    private final ProducaoColheitaRepository producaoColheitaRepository;
    private final SafraRepository safraRepository;
    private final SafraService safraService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<EtapaCultivoDTO.EtapaCultivoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return etapaCultivoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public EtapaCultivoDTO.EtapaCultivoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public EtapaCultivoDTO.EtapaCultivoResponse cadastrar(
            EtapaCultivoDTO.EtapaCultivoRequest request) {
        contexto.bloquearFazenda();

        EtapaCultivo entidade = new EtapaCultivo();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = etapaCultivoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "EtapaCultivo", entidade.getEtapaCultivoId());
        return converterParaResponse(entidade);
    }

    private EtapaCultivo buscarEntidadePorId(Long id) {
        EtapaCultivo entidade =
                etapaCultivoRepository
                        .findByEtapaCultivoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "EtapaCultivo não encontrado nesta fazenda."));

        return entidade;
    }

    private EtapaCultivoDTO.EtapaCultivoResponse converterParaResponse(EtapaCultivo entidade) {
        EtapaCultivoDTO.EtapaCultivoResponse response = new EtapaCultivoDTO.EtapaCultivoResponse();
        response.setEtapaCultivoId(entidade.getEtapaCultivoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setEtapa(entidade.getEtapa());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setObservacoes(entidade.getObservacoes());
        return response;
    }

    private void aplicarRequestNaEntidade(
            EtapaCultivo entidade, EtapaCultivoDTO.EtapaCultivoRequest request) {
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
        entidade.setEtapa(request.getEtapa().trim());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setObservacoes(request.getObservacoes());
        if (entidade.getSafra() == null) {
            if (request.getNovaSafra() == null || !"PREPARO".equals(entidade.getEtapa()))
                throw erro("Novo ciclo exige os dados da safra e primeiro preparo.");
            request.getNovaSafra().setDtaInicio(entidade.getDtaOperacao());
            Long id = safraService.cadastrar(request.getNovaSafra()).getSafraId();
            entidade.setSafra(safraRepository.findById(id).orElseThrow());
        } else if (request.getNovaSafra() != null)
            throw erro("Informe safra existente ou novo ciclo, não ambos.");
        Safra safra = safraRepository.bloquear(entidade.getSafra().getSafraId()).orElseThrow();
        var historico =
                etapaCultivoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .filter(e -> e.getSafra().getSafraId().equals(safra.getSafraId()))
                        .sorted(
                                Comparator.comparing(EtapaCultivo::getDtaOperacao)
                                        .thenComparing(EtapaCultivo::getEtapaCultivoId))
                        .toList();
        String ultima =
                historico.isEmpty() ? "PLANEJADA" : historico.get(historico.size() - 1).getEtapa();
        String esperada =
                switch (ultima) {
                    case "PLANEJADA" -> "PREPARO";
                    case "PREPARO" -> "PLANTIO";
                    case "PLANTIO" -> "COLHEITA";
                    case "COLHEITA" -> "ENCERRAMENTO";
                    default -> "";
                };
        if (!entidade.getEtapa().equals(esperada))
            throw erro("Etapa fora de sequência ou ciclo encerrado.");
        if (!historico.isEmpty()
                && entidade.getDtaOperacao()
                        .isBefore(historico.get(historico.size() - 1).getDtaOperacao()))
            throw erro("Datas das etapas devem ser não decrescentes.");
        if (entidade.getEtapa().equals("ENCERRAMENTO")) {
            var producoes =
                    producaoColheitaRepository
                            .findAllByFazendaFazendaId(contexto.codFazenda())
                            .stream()
                            .filter(p -> p.getSafra().getSafraId().equals(safra.getSafraId()))
                            .toList();
            if (producoes.isEmpty()
                    || producoes.stream()
                            .anyMatch(p -> p.getDtaOperacao().isAfter(entidade.getDtaOperacao())))
                throw erro(
                        "Encerramento exige produção ou perda documentada e data posterior aos"
                            + " registros.");
        }
        safra.setStatus(
                entidade.getEtapa().equals("ENCERRAMENTO") ? "ENCERRADA" : entidade.getEtapa());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
