package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "CalendarioSanitario")
@Table(name = "calendarios_sanitarios")
public class CalendarioSanitario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long calendarioSanitarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "especie", nullable = true, length = 2000)
    public String especie;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_lote", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_produto", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public ItemEstoque produto;

    @Column(name = "acao", nullable = false, length = 2000)
    public String acao;

    @Column(name = "orientacao_tecnica", nullable = false, length = 2000)
    public String orientacaoTecnica;

    @Column(name = "dta_prevista", nullable = true)
    public LocalDate dtaPrevista;

    @Column(name = "intervalo_dias", nullable = true)
    public Integer intervaloDias;
}
