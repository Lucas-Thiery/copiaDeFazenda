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
public class ItemEstoqueService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final PartidaEstoqueRepository partidaEstoqueRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<ItemEstoqueDTO.ItemEstoqueResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return itemEstoqueRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public ItemEstoqueDTO.ItemEstoqueResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ItemEstoqueDTO.ItemEstoqueResponse cadastrar(ItemEstoqueDTO.ItemEstoqueRequest request) {
        contexto.bloquearFazenda();
        if (itemEstoqueRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(e -> e.getNome().equalsIgnoreCase(request.getNome().trim())))
            throw erro("ItemEstoque já cadastrado.");
        ItemEstoque entidade = new ItemEstoque();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = itemEstoqueRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "ItemEstoque", entidade.getItemEstoqueId());
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ItemEstoqueDTO.ItemEstoqueResponse atualizar(
            Long id, ItemEstoqueDTO.ItemEstoqueRequest request) {
        contexto.bloquearFazenda();
        ItemEstoque entidade = buscarEntidadePorId(id);
        aplicarRequestNaEntidade(entidade, request);
        entidade = itemEstoqueRepository.saveAndFlush(entidade);
        auditoriaService.registrar("ALTERACAO", "ItemEstoque", id);
        return converterParaResponse(entidade);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void deletar(Long id) {
        contexto.bloquearFazenda();
        ItemEstoque entidade = buscarEntidadePorId(id);
        itemEstoqueRepository.delete(entidade);
        itemEstoqueRepository.flush();
        auditoriaService.registrar("EXCLUSAO", "ItemEstoque", id);
    }

    private ItemEstoque buscarEntidadePorId(Long id) {
        ItemEstoque entidade =
                itemEstoqueRepository
                        .findByItemEstoqueIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "ItemEstoque não encontrado nesta fazenda."));

        return entidade;
    }

    private ItemEstoqueDTO.ItemEstoqueResponse converterParaResponse(ItemEstoque entidade) {
        ItemEstoqueDTO.ItemEstoqueResponse response = new ItemEstoqueDTO.ItemEstoqueResponse();
        response.setItemEstoqueId(entidade.getItemEstoqueId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodigo(entidade.getCodigo());
        response.setNome(entidade.getNome());
        response.setCategoria(entidade.getCategoria());
        response.setUnidadeMedida(entidade.getUnidadeMedida());
        response.setEstoqueMinimo(entidade.getEstoqueMinimo());
        response.setAtivo(entidade.isAtivo());
        BigDecimal saldo =
                partidaEstoqueRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .filter(
                                p ->
                                        p.getItem()
                                                .getItemEstoqueId()
                                                .equals(entidade.getItemEstoqueId()))
                        .map(PartidaEstoque::getQuantidadeAtual)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setSaldo(saldo);
        response.setAbaixoMinimo(saldo.compareTo(entidade.getEstoqueMinimo()) < 0);
        return response;
    }

    private void aplicarRequestNaEntidade(
            ItemEstoque entidade, ItemEstoqueDTO.ItemEstoqueRequest request) {
        entidade.setCodigo(request.getCodigo());
        entidade.setNome(request.getNome().trim());
        entidade.setCategoria(request.getCategoria().trim());
        entidade.setUnidadeMedida(request.getUnidadeMedida().trim());
        entidade.setEstoqueMinimo(request.getEstoqueMinimo());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
