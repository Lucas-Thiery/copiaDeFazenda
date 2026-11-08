package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Aplicacao")
@Table(name = "aplicacoes")
public class Aplicacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long aplicacaoId;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_safra", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Safra safra;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_animal", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Animal animal;

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
    @JoinColumn(name = "cod_manejo", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Manejo manejo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_agendamento", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public AgendamentoSanitario agendamento;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    public BigDecimal quantidade;

    @Column(name = "unidade", nullable = false, length = 2000)
    public String unidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_responsavel", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Funcionario responsavel;

    @Column(name = "dosagem", nullable = true, length = 2000)
    public String dosagem;

    @Column(name = "orientacao_tecnica", nullable = true, length = 2000)
    public String orientacaoTecnica;

    @Column(name = "carencia_dias", nullable = true)
    public Integer carenciaDias;

    @Column(name = "custo", nullable = true, precision = 19, scale = 4)
    public BigDecimal custo;

    @Column(name = "referencia", nullable = false, length = 2000)
    public String referencia;
}
