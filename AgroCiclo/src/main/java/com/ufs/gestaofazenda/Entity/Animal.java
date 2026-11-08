package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Animal")
@Table(name = "animais")
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long animalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_lote", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lote loteAtual;

    @Column(name = "identificacao", nullable = false, length = 2000)
    public String identificacao;

    @Column(name = "especie", nullable = false, length = 2000)
    public String especie;

    @Column(name = "raca", nullable = false, length = 2000)
    public String raca;

    @Column(name = "peso_inicial_kg", nullable = false, precision = 19, scale = 4)
    public BigDecimal peso;

    @Column(name = "idade_meses", nullable = true)
    public Integer idadeMeses;

    @Column(name = "dta_nascimento", nullable = true)
    public LocalDate dtaNascimento;

    @Column(name = "data_entrada", nullable = false)
    public LocalDate dtaEntrada;

    @Column(name = "observacoes", nullable = true, length = 2000)
    public String observacoes;

    @Column(name = "ativo", nullable = true)
    public boolean ativo = true;
}
