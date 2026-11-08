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
public class AgendamentoSanitarioService {
    private final AgendamentoSanitarioRepository agendamentoSanitarioRepository;
    private final AnimalRepository animalRepository;
    private final AuditoriaService auditoriaService;
    private final CalendarioSanitarioRepository calendarioSanitarioRepository;
    private final ContextoUsuario contexto;
    private final FuncionarioRepository funcionarioRepository;
    private final LoteRepository loteRepository;
    private final RebanhoService rebanhoService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<AgendamentoSanitarioDTO.AgendamentoSanitarioResponse> listar(
            int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return agendamentoSanitarioRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public AgendamentoSanitarioDTO.AgendamentoSanitarioResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public AgendamentoSanitarioDTO.AgendamentoSanitarioResponse cadastrar(
            AgendamentoSanitarioDTO.AgendamentoSanitarioRequest request) {
        contexto.bloquearFazenda();

        AgendamentoSanitario entidade = new AgendamentoSanitario();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = agendamentoSanitarioRepository.saveAndFlush(entidade);

        auditoriaService.registrar(
                "INCLUSAO", "AgendamentoSanitario", entidade.getAgendamentoSanitarioId());
        return converterParaResponse(entidade);
    }

    private AgendamentoSanitario buscarEntidadePorId(Long id) {
        AgendamentoSanitario entidade =
                agendamentoSanitarioRepository
                        .findByAgendamentoSanitarioIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "AgendamentoSanitario não encontrado nesta"
                                                    + " fazenda."));

        return entidade;
    }

    private AgendamentoSanitarioDTO.AgendamentoSanitarioResponse converterParaResponse(
            AgendamentoSanitario entidade) {
        AgendamentoSanitarioDTO.AgendamentoSanitarioResponse response =
                new AgendamentoSanitarioDTO.AgendamentoSanitarioResponse();
        response.setAgendamentoSanitarioId(entidade.getAgendamentoSanitarioId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodCalendario(
                entidade.getCalendario() == null
                        ? null
                        : entidade.getCalendario().getCalendarioSanitarioId());
        response.setCodAnimal(
                entidade.getAnimal() == null ? null : entidade.getAnimal().getAnimalId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodResponsavel(
                entidade.getResponsavel() == null
                        ? null
                        : entidade.getResponsavel().getFuncionarioId());
        response.setDtaPrevista(entidade.getDtaPrevista());
        response.setConcluido(entidade.isConcluido());
        return response;
    }

    private void aplicarRequestNaEntidade(
            AgendamentoSanitario entidade,
            AgendamentoSanitarioDTO.AgendamentoSanitarioRequest request) {
        entidade.setCalendario(
                request.getCodCalendario() == null
                        ? null
                        : calendarioSanitarioRepository
                                .findById(request.getCodCalendario())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "CalendarioSanitario inexistente ou de"
                                                            + " outra fazenda.")));
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
        entidade.setResponsavel(
                request.getCodResponsavel() == null
                        ? null
                        : funcionarioRepository
                                .findById(request.getCodResponsavel())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "Funcionario inexistente ou de outra"
                                                            + " fazenda.")));
        entidade.setDtaPrevista(request.getDtaPrevista());
        if ((entidade.getAnimal() == null) == (entidade.getLote() == null))
            throw erro("Informe somente animal ou lote.");
        CalendarioSanitario calendario = entidade.getCalendario();
        Lote alvo =
                entidade.getAnimal() == null
                        ? entidade.getLote()
                        : entidade.getAnimal().getLoteAtual();
        if (calendario.getLote() != null
                && !calendario.getLote().getLoteId().equals(alvo.getLoteId()))
            throw erro("Calendário de outro lote.");
        if (calendario.getEspecie() != null && !calendario.getEspecie().isBlank()) {
            if (entidade.getAnimal() != null
                    && !calendario.getEspecie().equalsIgnoreCase(entidade.getAnimal().getEspecie()))
                throw erro("Espécie incompatível.");
        }
        if (calendario.getDtaPrevista() != null
                && !entidade.getDtaPrevista().equals(calendario.getDtaPrevista()))
            throw erro("Data diverge do calendário técnico.");
        if (entidade.getAnimal() == null
                && calendario.getEspecie() != null
                && !calendario.getEspecie().isBlank()) {
            var membros = rebanhoService.composicao(entidade.getLote(), LocalDate.now());
            if (membros.isEmpty()
                    || membros.stream()
                            .map(animalRepository::findById)
                            .map(Optional::orElseThrow)
                            .anyMatch(
                                    a -> !a.getEspecie().equalsIgnoreCase(calendario.getEspecie())))
                throw erro("Espécie do lote não corresponde ao calendário.");
        }
        var anteriores =
                agendamentoSanitarioRepository
                        .findAllByFazendaFazendaId(contexto.codFazenda())
                        .stream()
                        .filter(
                                a ->
                                        a.getCalendario()
                                                .getCalendarioSanitarioId()
                                                .equals(calendario.getCalendarioSanitarioId()))
                        .filter(
                                a ->
                                        Objects.equals(
                                                        a.getAnimal() == null
                                                                ? null
                                                                : a.getAnimal().getAnimalId(),
                                                        entidade.getAnimal() == null
                                                                ? null
                                                                : entidade.getAnimal()
                                                                        .getAnimalId())
                                                && Objects.equals(
                                                        a.getLote() == null
                                                                ? null
                                                                : a.getLote().getLoteId(),
                                                        entidade.getLote() == null
                                                                ? null
                                                                : entidade.getLote().getLoteId()))
                        .toList();
        if (anteriores.stream().anyMatch(a -> a.getDtaPrevista().equals(entidade.getDtaPrevista())))
            throw erro("Agendamento já existe nesta data.");
        if (calendario.getIntervaloDias() != null && !anteriores.isEmpty()) {
            LocalDate ultima =
                    anteriores.stream()
                            .map(AgendamentoSanitario::getDtaPrevista)
                            .max(LocalDate::compareTo)
                            .orElseThrow();
            if (!entidade.getDtaPrevista().equals(ultima.plusDays(calendario.getIntervaloDias())))
                throw erro("Data diverge do intervalo técnico registrado.");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
