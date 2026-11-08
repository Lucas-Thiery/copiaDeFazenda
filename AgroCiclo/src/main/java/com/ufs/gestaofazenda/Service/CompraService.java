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
public class CompraService {
    private final AuditoriaService auditoriaService;
    private final CompraRepository compraRepository;
    private final ContextoUsuario contexto;
    private final FornecedorRepository fornecedorRepository;
    private final ItemCompraRepository itemCompraRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final LancamentoService lancamentoService;
    private final PartidaEstoqueService partidaEstoqueService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public List<CompraDTO.CompraResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return compraRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public CompraDTO.CompraResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public CompraDTO.CompraResponse cadastrar(CompraDTO.CompraRequest request) {
        contexto.bloquearFazenda();

        Compra entidade = new Compra();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = compraRepository.saveAndFlush(entidade);
        int indice = 0;
        for (var itemRequest : request.getItens()) {
            ItemEstoque item =
                    itemEstoqueRepository
                            .findByItemEstoqueIdAndFazendaFazendaId(
                                    itemRequest.getCodItem(), contexto.codFazenda())
                            .orElseThrow(() -> erro("Item de outra fazenda ou inexistente."));
            if (!itemRequest.getUnidade().equals(item.getUnidadeMedida()) || !item.isAtivo())
                throw erro("Unidade incompatível ou item inativo.");
            ItemCompra detalhe = new ItemCompra();
            detalhe.setFazenda(entidade.getFazenda());
            detalhe.setCompra(entidade);
            detalhe.setItem(item);
            detalhe.setQuantidade(itemRequest.getQuantidade());
            detalhe.setUnidade(itemRequest.getUnidade());
            detalhe.setValorUnitario(itemRequest.getValorUnitario());
            detalhe.setCodigoLote(itemRequest.getCodigoLote());
            detalhe.setDtaValidade(itemRequest.getDtaValidade());
            itemCompraRepository.save(detalhe);
            partidaEstoqueService.receberCompra(entidade, detalhe, ++indice);
        }
        lancamentoService.obrigacaoCompra(entidade);
        auditoriaService.registrar("INCLUSAO", "Compra", entidade.getCompraId());
        return converterParaResponse(entidade);
    }

    private Compra buscarEntidadePorId(Long id) {
        Compra entidade =
                compraRepository
                        .findByCompraIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Compra não encontrado nesta fazenda."));

        return entidade;
    }

    private CompraDTO.CompraResponse converterParaResponse(Compra entidade) {
        CompraDTO.CompraResponse response = new CompraDTO.CompraResponse();
        response.setCompraId(entidade.getCompraId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodFornecedor(
                entidade.getFornecedor() == null
                        ? null
                        : entidade.getFornecedor().getFornecedorId());
        response.setReferencia(entidade.getReferencia());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setTotal(entidade.getTotal());
        return response;
    }

    private void aplicarRequestNaEntidade(Compra entidade, CompraDTO.CompraRequest request) {
        entidade.setFornecedor(
                request.getCodFornecedor() == null
                        ? null
                        : fornecedorRepository
                                .findById(request.getCodFornecedor())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Fornecedor inexistente ou de outra fazenda.")));
        entidade.setReferencia(request.getReferencia().trim());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setTotal(request.getTotal());
        if (compraRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        c ->
                                c.getFornecedor()
                                                .getFornecedorId()
                                                .equals(entidade.getFornecedor().getFornecedorId())
                                        && c.getReferencia().equals(entidade.getReferencia())))
            throw erro("Compra já registrada para o fornecedor.");
        BigDecimal calculado =
                request.getItens().stream()
                        .map(i -> i.getQuantidade().multiply(i.getValorUnitario()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (calculado.compareTo(entidade.getTotal()) != 0)
            throw erro("Total divergente da soma dos itens.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
