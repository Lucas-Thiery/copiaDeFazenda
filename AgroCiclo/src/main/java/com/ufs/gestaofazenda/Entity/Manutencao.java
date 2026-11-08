package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Manutencao")
@Table(name = "manutencoes")
public class Manutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long manutencaoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_maquina", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_plano", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public PlanoManutencao plano;

    @Column(name = "tipo", nullable = false, length = 2000)
    public String tipo;

    @Column(name = "servico", nullable = false, length = 2000)
    public String servico;

    @Column(name = "pecas", nullable = true, length = 2000)
    public String pecas;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "custo", nullable = false, precision = 19, scale = 4)
    public BigDecimal custo;

    @Column(name = "referencia", nullable = false, length = 2000)
    public String referencia;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_lancamento", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lancamento lancamento;
}
