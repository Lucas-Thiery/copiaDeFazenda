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
public class ItemCompraService {
    private final AuditoriaService auditoriaService;
    private final CompraRepository compraRepository;
    private final ContextoUsuario contexto;
    private final ItemCompraRepository itemCompraRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<ItemCompraDTO.ItemCompraResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return itemCompraRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public ItemCompraDTO.ItemCompraResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    private ItemCompra buscarEntidadePorId(Long id) {
        ItemCompra entidade =
                itemCompraRepository
                        .findByItemCompraIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "ItemCompra não encontrado nesta fazenda."));

        return entidade;
    }

    private ItemCompraDTO.ItemCompraResponse converterParaResponse(ItemCompra entidade) {
        ItemCompraDTO.ItemCompraResponse response = new ItemCompraDTO.ItemCompraResponse();
        response.setItemCompraId(entidade.getItemCompraId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodCompra(
                entidade.getCompra() == null ? null : entidade.getCompra().getCompraId());
        response.setCodItem(
                entidade.getItem() == null ? null : entidade.getItem().getItemEstoqueId());
        response.setQuantidade(entidade.getQuantidade());
        response.setUnidade(entidade.getUnidade());
        response.setValorUnitario(entidade.getValorUnitario());
        response.setCodigoLote(entidade.getCodigoLote());
        response.setDtaValidade(entidade.getDtaValidade());
        return response;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
