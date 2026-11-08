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
public class MaquinaService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final MaquinaRepository maquinaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<MaquinaDTO.MaquinaResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return maquinaRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public MaquinaDTO.MaquinaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public MaquinaDTO.MaquinaResponse cadastrar(MaquinaDTO.MaquinaRequest request) {
        contexto.bloquearFazenda();
        if (maquinaRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Maquina já cadastrado.");
        Maquina entidade = new Maquina();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = maquinaRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Maquina", entidade.getMaquinaId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public MaquinaDTO.MaquinaResponse atualizar(Long id, MaquinaDTO.MaquinaRequest request) {
        contexto.bloquearFazenda();
        Maquina entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = maquinaRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "Maquina", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        Maquina entidade = buscarEntidadePorId(id);
        maquinaRepository.delete(entidade);
        maquinaRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "Maquina", id);
    }

    private Maquina buscarEntidadePorId(Long id) {
        Maquina entidade =
                maquinaRepository
                        .findByMaquinaIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Maquina não encontrado nesta fazenda."));

        return entidade;
    }

    private MaquinaDTO.MaquinaResponse converterParaResponse(Maquina entidade) {
        MaquinaDTO.MaquinaResponse response = new MaquinaDTO.MaquinaResponse();
        response.setMaquinaId(entidade.getMaquinaId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setModelo(entidade.getModelo());
        response.setAno(entidade.getAno());
        response.setChassi(entidade.getChassi());
        response.setAbastecivel(entidade.isAbastecivel());
        response.setUnidadeMedidor(entidade.getUnidadeMedidor());
        response.setLeitura(entidade.getLeitura());
        response.setDtaLeitura(entidade.getDtaLeitura());
        return response;
    }

    private void aplicarRequestNaEntidade(Maquina entidade, MaquinaDTO.MaquinaRequest request) {
        String anteriorUnidade = entidade.getUnidadeMedidor();
        BigDecimal anteriorLeitura = entidade.getLeitura();
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setModelo(request.getModelo().trim());
        entidade.setAno(request.getAno());
        entidade.setChassi(request.getChassi());
        entidade.setAbastecivel(request.isAbastecivel());
        entidade.setUnidadeMedidor(request.getUnidadeMedidor());
        entidade.setLeitura(request.getLeitura());
        entidade.setDtaLeitura(request.getDtaLeitura());
        if (entidade.getUnidadeMedidor() != null && entidade.getLeitura() == null)
            throw erro("Informe a leitura inicial do medidor.");
        if (entidade.getAno() > LocalDate.now().getYear() + 1) throw erro("Ano inválido.");
        if (entidade.getChassi() != null
                && !entidade.getChassi().isBlank()
                && maquinaRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .anyMatch(
                                m ->
                                        entidade.getChassi().equalsIgnoreCase(m.getChassi())
                                                && !Objects.equals(
                                                        m.getMaquinaId(), entidade.getMaquinaId())))
            throw erro("Chassi já cadastrado.");
        if (entidade.getMaquinaId() != null
                && (!Objects.equals(anteriorUnidade, entidade.getUnidadeMedidor())
                        || !Objects.equals(anteriorLeitura, entidade.getLeitura())))
            throw erro("Medidor e leitura devem ser preservados; registre o uso.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
