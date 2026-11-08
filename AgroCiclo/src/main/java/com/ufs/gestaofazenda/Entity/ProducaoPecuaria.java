package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "ProducaoPecuaria")
@Table(name = "producoes_pecuarias")
public class ProducaoPecuaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long producaoPecuariaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

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

    @Column(name = "produto", nullable = false, length = 2000)
    public String produto;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    public BigDecimal quantidade;

    @Column(name = "unidade", nullable = false, length = 2000)
    public String unidade;

    @Column(name = "destinacao", nullable = true, length = 2000)
    public String destinacao;

    @Column(name = "aviso_carencia", nullable = true, length = 2000)
    public String avisoCarencia;
}
