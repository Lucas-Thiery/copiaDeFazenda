package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.FluxoCaixaDTO;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROPRIETARIO')")
public class FluxoCaixaService {
    private final LancamentoRepository lancamentoRepository;
    private final LiquidacaoRepository liquidacaoRepository;
    private final ContextoUsuario contexto;

    @Transactional(readOnly = true)
    public FluxoCaixaDTO.FluxoCaixaResponse consultar(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido.");
        var response = new FluxoCaixaDTO.FluxoCaixaResponse();
        response.setDtaInicio(inicio);
        response.setDtaFim(fim);
        BigDecimal entradas = BigDecimal.ZERO, saidas = BigDecimal.ZERO, inicial = BigDecimal.ZERO;
        var liquidacoes = liquidacaoRepository.findAllByFazendaFazendaId(contexto.codFazenda());
        for (var l : liquidacoes) {
            if (l.getDtaOperacao().isBefore(inicio))
                inicial =
                        inicial.add(
                                l.getLancamento().getTipo().equals("RECEITA")
                                        ? l.getValor()
                                        : l.getValor().negate());
            else if (!l.getDtaOperacao().isAfter(fim)) {
                if (l.getLancamento().getTipo().equals("RECEITA"))
                    entradas = entradas.add(l.getValor());
                else saidas = saidas.add(l.getValor());
            }
        }
        BigDecimal receitas = BigDecimal.ZERO, despesas = BigDecimal.ZERO;
        for (var l : lancamentoRepository.findAllByFazendaFazendaId(contexto.codFazenda())) {
            if (l.getDtaOperacao().isAfter(fim)) continue;
            BigDecimal pago =
                    liquidacoes.stream()
                            .filter(
                                    q ->
                                            q.getLancamento()
                                                            .getLancamentoId()
                                                            .equals(l.getLancamentoId())
                                                    && !q.getDtaOperacao().isAfter(fim))
                            .map(q -> q.getValor())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal saldo = l.getValor().subtract(pago);
            if (l.getTipo().equals("RECEITA")) receitas = receitas.add(saldo);
            else despesas = despesas.add(saldo);
        }
        response.setSaldoInicialRegistrado(inicial);
        response.setRecebimentos(entradas);
        response.setPagamentos(saidas);
        response.setSaldoPeriodo(entradas.subtract(saidas));
        response.setSaldoFinalRegistrado(inicial.add(entradas).subtract(saidas));
        response.setReceitasPendentes(receitas);
        response.setDespesasPendentes(despesas);
        response.setCriterio(
                "Somente liquidações movimentam caixa; saldo inicial considera apenas registros"
                    + " disponíveis, sem presumir saldo bancário.");
        return response;
    }
}
