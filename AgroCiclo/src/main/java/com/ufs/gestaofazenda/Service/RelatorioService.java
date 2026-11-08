package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.RelatorioDTO;
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
public class RelatorioService {
    private final ContextoUsuario contexto;
    private final CustoService custoService;
    private final SafraRepository safraRepository;
    private final LoteRepository loteRepository;
    private final AnimalRepository animalRepository;
    private final AplicacaoRepository aplicacaoRepository;
    private final ProducaoColheitaRepository producaoColheitaRepository;
    private final ProducaoPecuariaRepository producaoPecuariaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final LiquidacaoRepository liquidacaoRepository;

    @Transactional(readOnly = true)
    public RelatorioDTO.RelatorioResponse gerar(
            String tipo,
            LocalDate inicio,
            LocalDate fim,
            Long codSafra,
            Long codLote,
            Long codAnimal) {
        if (fim.isBefore(inicio)) throw erro("Período inválido.");
        Long fazenda = contexto.codFazenda();
        if (codSafra != null
                && safraRepository.findBySafraIdAndFazendaFazendaId(codSafra, fazenda).isEmpty())
            throw erro("Safra não autorizada.");
        if (codLote != null
                && loteRepository.findByLoteIdAndFazendaFazendaId(codLote, fazenda).isEmpty())
            throw erro("Lote não autorizado.");
        if (codAnimal != null
                && animalRepository.findByAnimalIdAndFazendaFazendaId(codAnimal, fazenda).isEmpty())
            throw erro("Animal não autorizado.");
        var r = new RelatorioDTO.RelatorioResponse();
        r.setTipo(tipo);
        r.setCodFazenda(fazenda);
        r.setDtaInicio(inicio);
        r.setDtaFim(fim);
        if (codSafra != null) r.getFiltros().put("codSafra", codSafra);
        if (codLote != null) r.getFiltros().put("codLote", codLote);
        if (codAnimal != null) r.getFiltros().put("codAnimal", codAnimal);
        switch (tipo) {
            case "custos" -> {
                if (codAnimal != null) throw erro("Custos não são apurados por animal individual.");
                r.setColunas(
                        List.of(
                                "destino",
                                "codigo",
                                "insumos",
                                "maoObra",
                                "despesas",
                                "total",
                                "denominador",
                                "indicador",
                                "pendencias"));
                if (codLote == null)
                    for (var s : safraRepository.findAllByFazendaFazendaId(fazenda)) {
                        if (codSafra != null && !s.getSafraId().equals(codSafra)) continue;
                        custo(r, "SAFRA", s.getSafraId(), inicio, fim);
                    }
                if (codSafra == null)
                    for (var l : loteRepository.findAllByFazendaFazendaId(fazenda)) {
                        if (codLote != null && !l.getLoteId().equals(codLote)) continue;
                        if (!fim.isBefore(l.getDtaFormacao()))
                            custo(r, "LOTE", l.getLoteId(), inicio, fim);
                    }
                r.getLimitacoes()
                        .add(
                                "Consumos + mão de obra preservada + despesas diretas; compras e"
                                    + " liquidações não são somadas novamente. Despesas gerais não"
                                    + " são rateadas.");
            }
            case "produtividade", "produtivo" -> {
                r.setColunas(
                        List.of(
                                "origem",
                                "codigo",
                                "produto",
                                "quantidade",
                                "unidade",
                                "data",
                                "areaHa",
                                "porHectare",
                                "situacao",
                                "codLoteHistorico",
                                "codOrigem"));
                if (codLote == null && codAnimal == null)
                    for (var p : producaoColheitaRepository.findAllByFazendaFazendaId(fazenda)) {
                        if (!periodo(p.getDtaOperacao(), inicio, fim)
                                || codSafra != null && !p.getSafra().getSafraId().equals(codSafra))
                            continue;
                        BigDecimal area = p.getSafra().getAreaPlantadaHa();
                        linha(
                                r,
                                "COLHEITA",
                                p.getProducaoColheitaId(),
                                p.getSafra().getCultura(),
                                p.getQuantidade(),
                                p.getUnidade(),
                                p.getDtaOperacao(),
                                area,
                                area != null && area.signum() > 0
                                        ? p.getQuantidade().divide(area, 4, RoundingMode.HALF_UP)
                                        : "não calculável",
                                p.getQuantidade().signum() == 0
                                        ? "PERDA TOTAL: " + p.getJustificativaPerda()
                                        : p.getSafra().getStatus(),
                                "",
                                p.getSafra().getSafraId());
                    }
                if (codSafra == null)
                    for (var p : producaoPecuariaRepository.findAllByFazendaFazendaId(fazenda)) {
                        if (!periodo(p.getDtaOperacao(), inicio, fim)
                                || codLote != null && !p.getLote().getLoteId().equals(codLote)
                                || codAnimal != null
                                        && (p.getAnimal() == null
                                                || !p.getAnimal().getAnimalId().equals(codAnimal)))
                            continue;
                        linha(
                                r,
                                p.getAnimal() == null ? "LOTE" : "ANIMAL",
                                p.getProducaoPecuariaId(),
                                p.getProduto(),
                                p.getQuantidade(),
                                p.getUnidade(),
                                p.getDtaOperacao(),
                                "",
                                "não definido",
                                p.getAvisoCarencia(),
                                p.getLote().getLoteId(),
                                p.getAnimal() == null
                                        ? p.getLote().getLoteId()
                                        : p.getAnimal().getAnimalId());
                    }
                r.getLimitacoes()
                        .add(
                                "Registros parciais separados por origem, produto, unidade e data."
                                    + " Não há soma de unidades incompatíveis ou denominador"
                                    + " pecuário presumido.");
            }
            case "sanitario", "aplicacoes" -> {
                r.setColunas(
                        List.of(
                                "aplicacaoId",
                                "codAnimal",
                                "codLote",
                                "codSafra",
                                "data",
                                "codPartida",
                                "quantidade",
                                "unidade",
                                "codResponsavel",
                                "carenciaDias",
                                "orientacaoTecnica"));
                for (var a : aplicacaoRepository.findAllByFazendaFazendaId(fazenda)) {
                    if (!periodo(a.getDtaOperacao(), inicio, fim)
                            || codSafra != null
                                    && (a.getSafra() == null
                                            || !a.getSafra().getSafraId().equals(codSafra))
                            || codLote != null
                                    && (a.getLote() == null
                                            || !a.getLote().getLoteId().equals(codLote))
                            || codAnimal != null
                                    && (a.getAnimal() == null
                                            || !a.getAnimal().getAnimalId().equals(codAnimal)))
                        continue;
                    boolean sanitario =
                            Set.of("VACINA", "MEDICAMENTO")
                                    .contains(a.getPartida().getItem().getCategoria());
                    if (tipo.equals("sanitario") && !sanitario) continue;
                    linha(
                            r,
                            a.getAplicacaoId(),
                            a.getAnimal() == null ? null : a.getAnimal().getAnimalId(),
                            a.getLote() == null ? null : a.getLote().getLoteId(),
                            a.getSafra() == null ? null : a.getSafra().getSafraId(),
                            a.getDtaOperacao(),
                            a.getPartida().getPartidaEstoqueId(),
                            a.getQuantidade(),
                            a.getUnidade(),
                            a.getResponsavel().getFuncionarioId(),
                            a.getCarenciaDias(),
                            a.getOrientacaoTecnica());
                }
            }
            case "financeiro" -> {
                if (codAnimal != null) throw erro("Filtro animal não aplicável ao financeiro.");
                r.setColunas(
                        List.of(
                                "lancamentoId",
                                "tipo",
                                "data",
                                "referencia",
                                "emitente",
                                "valor",
                                "liquidadoAteFim",
                                "saldoAteFim"));
                var liquidacoes = liquidacaoRepository.findAllByFazendaFazendaId(fazenda);
                for (var l : lancamentoRepository.findAllByFazendaFazendaId(fazenda)) {
                    if (!periodo(l.getDtaOperacao(), inicio, fim)
                            || codSafra != null
                                    && (l.getSafra() == null
                                            || !l.getSafra().getSafraId().equals(codSafra))
                            || codLote != null
                                    && (l.getLote() == null
                                            || !l.getLote().getLoteId().equals(codLote))) continue;
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
                    linha(
                            r,
                            l.getLancamentoId(),
                            l.getTipo(),
                            l.getDtaOperacao(),
                            l.getReferencia(),
                            l.getEmitente(),
                            l.getValor(),
                            pago,
                            l.getValor().subtract(pago));
                }
                r.getLimitacoes()
                        .add("Layout contábil provisório; aguarda validação DP06 com o contador.");
            }
            default -> throw erro("Tipo de relatório inválido.");
        }
        if (tipo.equals("produtividade")) consolidarProducao(r);
        if (r.getLinhas().isEmpty())
            r.getLimitacoes()
                    .add(
                            "Nenhum registro encontrado; ausência de dados não equivale a perda ou"
                                + " custo zero real.");
        return r;
    }

    private void custo(
            RelatorioDTO.RelatorioResponse r,
            String tipo,
            Long id,
            LocalDate inicio,
            LocalDate fim) {
        var c = custoService.apurar(tipo, id, inicio, fim);
        linha(
                r,
                tipo,
                id,
                c.getInsumos(),
                c.getMaoObra(),
                c.getDespesas(),
                c.getTotal(),
                c.getDenominador(),
                c.getCustoPorUnidade(),
                String.join("; ", c.getPendencias()));
    }

    private void linha(RelatorioDTO.RelatorioResponse r, Object... valores) {
        r.getLinhas().add(Arrays.stream(valores).map(v -> v == null ? "" : v.toString()).toList());
    }

    private boolean periodo(LocalDate data, LocalDate inicio, LocalDate fim) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    private void consolidarProducao(RelatorioDTO.RelatorioResponse relatorio) {
        Map<List<String>, List<List<String>>> grupos = new LinkedHashMap<>();
        for (var linha : relatorio.getLinhas()) {
            // Source, product, unit, preserved area, and historical lot define compatible groups.
            var chave =
                    List.of(
                            linha.get(0),
                            linha.get(10),
                            linha.get(2),
                            linha.get(4),
                            linha.get(6),
                            linha.get(9));
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(linha);
        }
        List<List<String>> consolidadas = new ArrayList<>();
        for (var grupo : grupos.values()) {
            List<String> linha = new ArrayList<>(grupo.get(0));
            BigDecimal total =
                    grupo.stream()
                            .map(l -> new BigDecimal(l.get(3)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            linha.set(1, "consolidado");
            linha.set(3, total.toPlainString());
            linha.set(5, relatorio.getDtaInicio() + " a " + relatorio.getDtaFim());
            if (!linha.get(6).isBlank()) {
                BigDecimal area = new BigDecimal(linha.get(6));
                linha.set(
                        7,
                        area.signum() > 0
                                ? total.divide(area, 4, RoundingMode.HALF_UP).toPlainString()
                                : "não calculável");
            }
            consolidadas.add(linha);
        }
        relatorio.setLinhas(consolidadas);
    }
}
