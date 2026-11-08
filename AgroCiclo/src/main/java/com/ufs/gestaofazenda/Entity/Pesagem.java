package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Pesagem")
@Table(name = "pesagens_animais")
public class Pesagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long pesagemId;

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

    @Column(name = "data_pesagem", nullable = false)
    public LocalDate dtaPesagem;

    @Column(name = "peso_kg", nullable = false, precision = 19, scale = 4)
    public BigDecimal peso;

    @Column(name = "composicao", nullable = true, length = 2000)
    public String composicao;

    @Column(name = "observacao", nullable = true, length = 2000)
    public String observacao;
}
