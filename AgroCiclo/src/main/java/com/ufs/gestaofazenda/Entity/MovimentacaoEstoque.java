package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "MovimentacaoEstoque")
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long movimentacaoEstoqueId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_partida", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public PartidaEstoque partida;

    @Column(name = "tipo", nullable = false, length = 2000)
    public String tipo;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    public BigDecimal quantidade;

    @Column(name = "data_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "motivo", nullable = false, length = 2000)
    public String motivo;

    @Column(name = "valor_total_origem", nullable = true, precision = 19, scale = 4)
    public BigDecimal valorTotalOrigem;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_safra", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Safra safra;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_lote", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_tarefa", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Tarefa tarefa;

    @Column(name = "referencia_origem", nullable = true, length = 2000)
    public String referenciaOrigem;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_responsavel", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Funcionario responsavel;
}
