package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.CustoDTO;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROPRIETARIO')")
public class CustoService {
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final TarefaRepository tarefaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final SafraRepository safraRepository;
    private final LoteRepository loteRepository;
    private final UsoMaquinaRepository usoMaquinaRepository;
    private final ContextoUsuario contexto;
    private final RebanhoService rebanhoService;

    @Transactional(readOnly = true)
    public CustoDTO.CustoResponse apurar(String destino, Long id, LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) throw erro("Período inválido.");
        Long fazenda = contexto.codFazenda();
        CustoDTO.CustoResponse response = new CustoDTO.CustoResponse();
        response.setDestino(destino);
        response.setCodDestino(id);
        response.setDtaInicio(inicio);
        response.setDtaFim(fim);
        if (destino.equals("SAFRA")) {
            Safra safra =
                    safraRepository
                            .findBySafraIdAndFazendaFazendaId(id, fazenda)
                            .orElseThrow(() -> erro("Safra inexistente."));
            response.setDenominador(safra.getAreaPlantadaHa());
        } else if (destino.equals("LOTE")) {
            Lote lote =
                    loteRepository
                            .findByLoteIdAndFazendaFazendaId(id, fazenda)
                            .orElseThrow(() -> erro("Lote inexistente."));
            if (fim.isBefore(lote.getDtaFormacao()))
                throw erro("Data final anterior à formação do lote.");
            response.setDenominador(
                    BigDecimal.valueOf(rebanhoService.composicao(lote, fim).size()));
        } else if (destino.equals("TAREFA")) {
            tarefaRepository
                    .findByTarefaIdAndFazendaFazendaId(id, fazenda)
                    .orElseThrow(() -> erro("Tarefa inexistente."));
        } else throw erro("Destino deve ser SAFRA, LOTE ou TAREFA.");
        BigDecimal insumos =
                movimentacaoEstoqueRepository.findAllByFazendaFazendaId(fazenda).stream()
                        .filter(
                                m ->
                                        m.getTipo().equals("USO")
                                                && periodo(m.getDtaOperacao(), inicio, fim))
                        .filter(
                                m ->
                                        corresponde(
                                                destino,
                                                id,
                                                m.getSafra(),
                                                m.getLote(),
                                                m.getTarefa()))
                        .map(MovimentacaoEstoque::getValorTotalOrigem)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maoObra =
                tarefaRepository.findAllByFazendaFazendaId(fazenda).stream()
                        .filter(
                                t ->
                                        t.getStatus().equals("CONCLUIDA")
                                                && periodo(
                                                        t.getDtaFimReal().toLocalDate(),
                                                        inicio,
                                                        fim))
                        .filter(t -> corresponde(destino, id, t.getSafra(), t.getLote(), t))
                        .map(Tarefa::getCustoMaoObra)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despesas =
                lancamentoRepository.findAllByFazendaFazendaId(fazenda).stream()
                        .filter(
                                l ->
                                        l.getTipo().equals("DESPESA")
                                                && !l.isCompraEstoque()
                                                && periodo(l.getDtaOperacao(), inicio, fim))
                        .filter(
                                l ->
                                        corresponde(
                                                destino,
                                                id,
                                                l.getSafra(),
                                                l.getLote(),
                                                l.getTarefa()))
                        .map(Lancamento::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setInsumos(insumos);
        response.setMaoObra(maoObra);
        response.setDespesas(despesas);
        response.setTotal(insumos.add(maoObra).add(despesas));
        if (response.getDenominador() != null && response.getDenominador().signum() > 0)
            response.setCustoPorUnidade(
                    response.getTotal().divide(response.getDenominador(), 4, RoundingMode.HALF_UP));
        else if (!destino.equals("TAREFA"))
            response.getPendencias().add("Indicador não calculável: denominador ausente ou zero.");
        boolean uso =
                usoMaquinaRepository.findAllByFazendaFazendaId(fazenda).stream()
                        .anyMatch(
                                u ->
                                        periodo(u.getDtaOperacao(), inicio, fim)
                                                && corresponde(
                                                        destino,
                                                        id,
                                                        u.getSafra(),
                                                        u.getLote(),
                                                        u.getTarefa()));
        if (uso)
            response.getPendencias()
                    .add(
                            "Horas de uso sem tarifa não geram custo; apenas origens financeiras"
                                + " documentadas foram somadas.");
        if (response.getTotal().signum() == 0)
            response.getPendencias()
                    .add("Total registrado zero não comprova custo real inexistente.");
        return response;
    }

    public boolean corresponde(String tipo, Long id, Safra safra, Lote lote, Tarefa tarefa) {
        if (tipo.equals("TAREFA")) return tarefa != null && tarefa.getTarefaId().equals(id);
        if (tipo.equals("SAFRA"))
            return (safra != null && safra.getSafraId().equals(id))
                    || (tarefa != null
                            && tarefa.getSafra() != null
                            && tarefa.getSafra().getSafraId().equals(id));
        return (lote != null && lote.getLoteId().equals(id))
                || (tarefa != null
                        && tarefa.getLote() != null
                        && tarefa.getLote().getLoteId().equals(id));
    }

    private boolean periodo(LocalDate data, LocalDate inicio, LocalDate fim) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
