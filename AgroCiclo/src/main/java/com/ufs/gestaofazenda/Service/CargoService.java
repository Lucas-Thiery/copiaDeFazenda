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
public class CargoService {
    private final AuditoriaService auditoriaService;
    private final CargoRepository cargoRepository;
    private final ContextoUsuario contexto;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public List<CargoDTO.CargoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return cargoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public CargoDTO.CargoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public CargoDTO.CargoResponse cadastrar(CargoDTO.CargoRequest request) {
        contexto.bloquearFazenda();
        if (cargoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(e -> e.getNome().equalsIgnoreCase(request.getNome().trim())))
            throw erro("Cargo já cadastrado.");
        Cargo entidade = new Cargo();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = cargoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Cargo", entidade.getCargoId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public CargoDTO.CargoResponse atualizar(Long id, CargoDTO.CargoRequest request) {
        contexto.bloquearFazenda();
        Cargo entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = cargoRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Cargo", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Cargo entidade = buscarEntidadePorId(id);
        cargoRepository.delete(entidade);
        cargoRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Cargo", id);
    }

    private Cargo buscarEntidadePorId(Long id) {
        Cargo entidade =
                cargoRepository
                        .findByCargoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Cargo não encontrado nesta fazenda."));

        return entidade;
    }

    private CargoDTO.CargoResponse converterParaResponse(Cargo entidade) {
        CargoDTO.CargoResponse response = new CargoDTO.CargoResponse();
        response.setCargoId(entidade.getCargoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setNome(entidade.getNome());
        response.setAtribuicoes(entidade.getAtribuicoes());
        response.setCustoHoraReferencia(
                contexto.proprietario() ? entidade.getCustoHoraReferencia() : null);
        return response;
    }

    private void aplicarRequestNaEntidade(Cargo entidade, CargoDTO.CargoRequest request) {
        entidade.setNome(request.getNome().trim());
        entidade.setAtribuicoes(request.getAtribuicoes().trim());
        entidade.setCustoHoraReferencia(request.getCustoHoraReferencia());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
