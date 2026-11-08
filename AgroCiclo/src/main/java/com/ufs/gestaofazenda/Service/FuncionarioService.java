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
public class FuncionarioService {
    private final AuditoriaService auditoriaService;
    private final CargoRepository cargoRepository;
    private final ContextoUsuario contexto;
    private final FuncionarioRepository funcionarioRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<FuncionarioDTO.FuncionarioResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return funcionarioRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public FuncionarioDTO.FuncionarioResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public FuncionarioDTO.FuncionarioResponse cadastrar(FuncionarioDTO.FuncionarioRequest request) {
        contexto.bloquearFazenda();

        Funcionario entidade = new Funcionario();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = funcionarioRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Funcionario", entidade.getFuncionarioId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public FuncionarioDTO.FuncionarioResponse atualizar(
            Long id, FuncionarioDTO.FuncionarioRequest request) {
        contexto.bloquearFazenda();
        Funcionario entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = funcionarioRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Funcionario", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Funcionario entidade = buscarEntidadePorId(id);
        funcionarioRepository.delete(entidade);
        funcionarioRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Funcionario", id);
    }

    private Funcionario buscarEntidadePorId(Long id) {
        Funcionario entidade =
                funcionarioRepository
                        .findByFuncionarioIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Funcionario não encontrado nesta fazenda."));

        return entidade;
    }

    private FuncionarioDTO.FuncionarioResponse converterParaResponse(Funcionario entidade) {
        FuncionarioDTO.FuncionarioResponse response = new FuncionarioDTO.FuncionarioResponse();
        response.setFuncionarioId(entidade.getFuncionarioId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodCargo(entidade.getCargo() == null ? null : entidade.getCargo().getCargoId());
        response.setNome(entidade.getNome());
        response.setEquipe(entidade.getEquipe());
        response.setHorarioTrabalho(entidade.getHorarioTrabalho());
        response.setCustoHoraIndividual(
                contexto.proprietario() ? entidade.getCustoHoraIndividual() : null);
        response.setCustoHoraEfetivo(
                contexto.proprietario() ? entidade.getCustoHoraEfetivo() : null);
        response.setAtivo(entidade.isAtivo());
        return response;
    }

    private void aplicarRequestNaEntidade(
            Funcionario entidade, FuncionarioDTO.FuncionarioRequest request) {
        entidade.setCargo(
                request.getCodCargo() == null
                        ? null
                        : cargoRepository
                                .findById(request.getCodCargo())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(() -> erro("Cargo inexistente ou de outra fazenda.")));
        entidade.setNome(request.getNome().trim());
        entidade.setEquipe(request.getEquipe());
        entidade.setHorarioTrabalho(request.getHorarioTrabalho().trim());
        entidade.setCustoHoraIndividual(request.getCustoHoraIndividual());
        entidade.setCustoHoraEfetivo(
                request.getCustoHoraIndividual() != null
                        ? request.getCustoHoraIndividual()
                        : entidade.getCargo().getCustoHoraReferencia());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
