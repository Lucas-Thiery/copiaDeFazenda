package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "MovimentacaoPastagem")
@Table(name = "movimentacoes_pastagem")
public class MovimentacaoPastagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long movimentacaoPastagemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_lote", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_origem", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Pastagem origem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_destino", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Pastagem destino;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;
}
