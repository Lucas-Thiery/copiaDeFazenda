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
public class PartidaEstoqueService {
    private final AuditoriaService auditoriaService;
    private final CompraRepository compraRepository;
    private final ContextoUsuario contexto;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final PartidaEstoqueRepository partidaEstoqueRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<PartidaEstoqueDTO.PartidaEstoqueResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return partidaEstoqueRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public PartidaEstoqueDTO.PartidaEstoqueResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public PartidaEstoqueDTO.PartidaEstoqueResponse cadastrar(
            PartidaEstoqueDTO.PartidaEstoqueRequest request) {
        contexto.bloquearFazenda();

        PartidaEstoque entidade = new PartidaEstoque();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = partidaEstoqueRepository.saveAndFlush(entidade);
        registrarEntrada(entidade);
        auditoriaService.registrar("INCLUSAO", "PartidaEstoque", entidade.getPartidaEstoqueId());
        return converterParaResponse(entidade);
    }

    private PartidaEstoque buscarEntidadePorId(Long id) {
        PartidaEstoque entidade =
                partidaEstoqueRepository
                        .findByPartidaEstoqueIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "PartidaEstoque não encontrado nesta fazenda."));

        return entidade;
    }

    private PartidaEstoqueDTO.PartidaEstoqueResponse converterParaResponse(
            PartidaEstoque entidade) {
        PartidaEstoqueDTO.PartidaEstoqueResponse response =
                new PartidaEstoqueDTO.PartidaEstoqueResponse();
        response.setPartidaEstoqueId(entidade.getPartidaEstoqueId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodItem(
                entidade.getItem() == null ? null : entidade.getItem().getItemEstoqueId());
        response.setCodCompra(
                entidade.getCompra() == null ? null : entidade.getCompra().getCompraId());
        response.setCodigoLote(entidade.getCodigoLote());
        response.setDtaEntrada(entidade.getDtaEntrada());
        response.setDtaValidade(entidade.getDtaValidade());
        response.setQuantidadeInicial(entidade.getQuantidadeInicial());
        response.setQuantidadeAtual(entidade.getQuantidadeAtual());
        response.setValorUnitarioOrigem(
                contexto.proprietario() ? entidade.getValorUnitarioOrigem() : null);
        response.setReferenciaOrigem(entidade.getReferenciaOrigem());
        return response;
    }

    private void aplicarRequestNaEntidade(
            PartidaEstoque entidade, PartidaEstoqueDTO.PartidaEstoqueRequest request) {
        entidade.setItem(
                request.getCodItem() == null
                        ? null
                        : itemEstoqueRepository
                                .findById(request.getCodItem())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "ItemEstoque inexistente ou de outra"
                                                            + " fazenda.")));
        entidade.setCompra(
                request.getCodCompra() == null
                        ? null
                        : compraRepository
                                .findById(request.getCodCompra())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Compra inexistente ou de outra fazenda.")));
        entidade.setCodigoLote(request.getCodigoLote());
        entidade.setDtaEntrada(request.getDtaEntrada());
        entidade.setDtaValidade(request.getDtaValidade());
        entidade.setQuantidadeInicial(request.getQuantidadeInicial());
        entidade.setValorUnitarioOrigem(request.getValorUnitarioOrigem());
        entidade.setReferenciaOrigem(request.getReferenciaOrigem());
        if (!entidade.getItem().isAtivo()) throw erro("Item inativo.");
        if (!request.getUnidade().equals(entidade.getItem().getUnidadeMedida()))
            throw erro("Unidade incompatível.");
        if (entidade.getCompra() != null)
            throw erro("Entradas de compra devem ser registradas em compras.");
        if (entidade.getReferenciaOrigem() == null || entidade.getReferenciaOrigem().isBlank())
            throw erro("Informe a origem documentada da entrada.");
        if (partidaEstoqueRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(p -> entidade.getReferenciaOrigem().equals(p.getReferenciaOrigem())))
            throw erro("Origem de entrada já registrada.");
        if (entidade.getDtaValidade() != null
                && entidade.getDtaValidade().isBefore(entidade.getDtaEntrada()))
            throw erro("Validade anterior à entrada.");
        entidade.setQuantidadeAtual(entidade.getQuantidadeInicial());
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public void receberCompra(Compra compra, ItemCompra item, int indice) {
        if (item.getDtaValidade() != null
                && item.getDtaValidade().isBefore(compra.getDtaOperacao()))
            throw erro("Validade anterior ao recebimento.");
        PartidaEstoque partida = new PartidaEstoque();
        partida.setFazenda(compra.getFazenda());
        partida.setCompra(compra);
        partida.setItem(item.getItem());
        partida.setCodigoLote(item.getCodigoLote());
        partida.setDtaEntrada(compra.getDtaOperacao());
        partida.setDtaValidade(item.getDtaValidade());
        partida.setQuantidadeInicial(item.getQuantidade());
        partida.setQuantidadeAtual(item.getQuantidade());
        partida.setValorUnitarioOrigem(item.getValorUnitario());
        partida.setReferenciaOrigem("COMPRA:" + compra.getCompraId() + ":" + indice);
        partida = partidaEstoqueRepository.save(partida);
        registrarEntrada(partida);
        auditoriaService.registrar("ENTRADA", "PartidaEstoque", partida.getPartidaEstoqueId());
    }

    private void registrarEntrada(PartidaEstoque partida) {
        MovimentacaoEstoque movimento = new MovimentacaoEstoque();
        movimento.setFazenda(partida.getFazenda());
        movimento.setPartida(partida);
        movimento.setTipo("ENTRADA");
        movimento.setQuantidade(partida.getQuantidadeInicial());
        movimento.setDtaOperacao(partida.getDtaEntrada());
        movimento.setMotivo("Recebimento documentado");
        movimento.setValorTotalOrigem(
                partida.getQuantidadeInicial().multiply(partida.getValorUnitarioOrigem()));
        movimento.setReferenciaOrigem(partida.getReferenciaOrigem());
        movimentacaoEstoqueRepository.save(movimento);
    }
}
