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
public class LancamentoService {
    private final AuditoriaService auditoriaService;
    private final CarenciaService carenciaService;
    private final ContextoUsuario contexto;
    private final LancamentoRepository lancamentoRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final LoteRepository loteRepository;
    private final SafraRepository safraRepository;
    private final TarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public List<LancamentoDTO.LancamentoResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return lancamentoRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public LancamentoDTO.LancamentoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public LancamentoDTO.LancamentoResponse cadastrar(LancamentoDTO.LancamentoRequest request) {
        contexto.bloquearFazenda();

        Lancamento entidade = new Lancamento();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = lancamentoRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Lancamento", entidade.getLancamentoId());
        return converterParaResponse(entidade);
    }

    private Lancamento buscarEntidadePorId(Long id) {
        Lancamento entidade =
                lancamentoRepository
                        .findByLancamentoIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Lancamento não encontrado nesta fazenda."));

        return entidade;
    }

    private LancamentoDTO.LancamentoResponse converterParaResponse(Lancamento entidade) {
        LancamentoDTO.LancamentoResponse response = new LancamentoDTO.LancamentoResponse();
        response.setLancamentoId(entidade.getLancamentoId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setTipo(entidade.getTipo());
        response.setDtaOperacao(entidade.getDtaOperacao());
        response.setDescricao(entidade.getDescricao());
        response.setCategoria(entidade.getCategoria());
        response.setReferencia(entidade.getReferencia());
        response.setEmitente(entidade.getEmitente());
        response.setValor(entidade.getValor());
        response.setValorLiquidado(entidade.getValorLiquidado());
        response.setCodSafra(entidade.getSafra() == null ? null : entidade.getSafra().getSafraId());
        response.setCodLote(entidade.getLote() == null ? null : entidade.getLote().getLoteId());
        response.setCodTarefa(
                entidade.getTarefa() == null ? null : entidade.getTarefa().getTarefaId());
        response.setCompraEstoque(entidade.isCompraEstoque());
        response.setAvisoCarencia(entidade.getAvisoCarencia());
        return response;
    }

    private void aplicarRequestNaEntidade(
            Lancamento entidade, LancamentoDTO.LancamentoRequest request) {
        entidade.setTipo(request.getTipo().trim());
        entidade.setDtaOperacao(request.getDtaOperacao());
        entidade.setDescricao(request.getDescricao().trim());
        entidade.setCategoria(request.getCategoria().trim());
        entidade.setReferencia(request.getReferencia().trim());
        entidade.setEmitente(request.getEmitente().trim());
        entidade.setValor(request.getValor());
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
        entidade.setTarefa(
                request.getCodTarefa() == null
                        ? null
                        : tarefaRepository
                                .findById(request.getCodTarefa())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () -> erro("Tarefa inexistente ou de outra fazenda.")));
        validarDestino(entidade.getSafra(), entidade.getLote(), entidade.getTarefa());
        if (lancamentoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        l ->
                                l.getTipo().equals(entidade.getTipo())
                                        && l.getReferencia().equals(entidade.getReferencia())
                                        && l.getEmitente().equals(entidade.getEmitente())))
            throw erro("Origem financeira já registrada; consulte o lançamento existente.");
        if (entidade.getTipo().equals("RECEITA")
                && entidade.getCategoria().equalsIgnoreCase("VENDA"))
            entidade.setAvisoCarencia(
                    carenciaService.aviso(
                            entidade.getSafra(), entidade.getLote(), entidade.getDtaOperacao()));
    }

    private void validarDestino(Safra safra, Lote lote, Tarefa tarefa) {
        if (safra != null && lote != null) throw erro("Destinos simultâneos não permitidos.");
        if (tarefa != null) {
            if (safra != null
                    && (tarefa.getSafra() == null
                            || !safra.getSafraId().equals(tarefa.getSafra().getSafraId())))
                throw erro("Safra diverge da tarefa.");
            if (lote != null
                    && (tarefa.getLote() == null
                            || !lote.getLoteId().equals(tarefa.getLote().getLoteId())))
                throw erro("Lote diverge da tarefa.");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public void obrigacaoCompra(Compra compra) {
        Lancamento lancamento = new Lancamento();
        lancamento.setFazenda(compra.getFazenda());
        lancamento.setTipo("DESPESA");
        lancamento.setDtaOperacao(compra.getDtaOperacao());
        lancamento.setDescricao("Compra recebida");
        lancamento.setCategoria("COMPRA_ESTOQUE");
        lancamento.setReferencia(compra.getReferencia());
        lancamento.setEmitente("FORNECEDOR:" + compra.getFornecedor().getFornecedorId());
        lancamento.setValor(compra.getTotal());
        lancamento.setCompraEstoque(true);
        if (lancamentoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        l ->
                                l.getReferencia().equals(lancamento.getReferencia())
                                        && l.getEmitente().equals(lancamento.getEmitente())
                                        && l.getTipo().equals("DESPESA")))
            throw erro(
                    "Obrigação desta compra já contabilizada; concilie a origem antes de registrar"
                        + " o recebimento.");
        lancamentoRepository.save(lancamento);
        auditoriaService.registrar("INCLUSAO", "Lancamento", lancamento.getLancamentoId());
    }

    public Lancamento origemMaquina(
            Lancamento existente,
            String referencia,
            String categoria,
            BigDecimal valor,
            LocalDate data,
            Safra safra,
            Lote lote,
            Tarefa tarefa) {
        Lancamento lancamento = existente;
        if (lancamento == null) {
            lancamento =
                    lancamentoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                            .filter(
                                    l ->
                                            l.getTipo().equals("DESPESA")
                                                    && l.getReferencia().equals(referencia)
                                                    && l.getEmitente().equals("MAQUINA"))
                            .findFirst()
                            .orElse(null);
        }
        if (lancamento != null) {
            final Long origemId = lancamento.getLancamentoId();
            boolean vinculado =
                    abastecimentoRepository
                            .findAllByFazendaFazendaId(contexto.codFazenda())
                            .stream()
                            .anyMatch(
                                    a ->
                                            a.getLancamento() != null
                                                    && a.getLancamento()
                                                            .getLancamentoId()
                                                            .equals(origemId));
            vinculado |=
                    manutencaoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                            .anyMatch(
                                    m ->
                                            m.getLancamento() != null
                                                    && m.getLancamento()
                                                            .getLancamentoId()
                                                            .equals(origemId));
            if (vinculado) throw erro("Origem financeira já vinculada a uma operação de máquina.");
            if (!lancamento.getTipo().equals("DESPESA")
                    || lancamento.isCompraEstoque()
                    || lancamento.getValor().compareTo(valor) != 0
                    || !Objects.equals(lancamento.getReferencia(), referencia)
                    || !Objects.equals(
                            lancamento.getSafra() == null
                                    ? null
                                    : lancamento.getSafra().getSafraId(),
                            safra == null ? null : safra.getSafraId())
                    || !Objects.equals(
                            lancamento.getLote() == null ? null : lancamento.getLote().getLoteId(),
                            lote == null ? null : lote.getLoteId())
                    || !Objects.equals(
                            lancamento.getTarefa() == null
                                    ? null
                                    : lancamento.getTarefa().getTarefaId(),
                            tarefa == null ? null : tarefa.getTarefaId()))
                throw erro("Origem financeira incompatível com o valor ou destino.");
            return lancamento;
        }
        lancamento = new Lancamento();
        lancamento.setFazenda(contexto.fazenda());
        lancamento.setTipo("DESPESA");
        lancamento.setReferencia(referencia);
        lancamento.setEmitente("MAQUINA");
        lancamento.setCategoria(categoria);
        lancamento.setDescricao(categoria);
        lancamento.setDtaOperacao(data);
        lancamento.setValor(valor);
        lancamento.setSafra(safra);
        lancamento.setLote(lote);
        lancamento.setTarefa(tarefa);
        lancamento = lancamentoRepository.save(lancamento);
        auditoriaService.registrar("INCLUSAO", "Lancamento", lancamento.getLancamentoId());
        return lancamento;
    }
}
