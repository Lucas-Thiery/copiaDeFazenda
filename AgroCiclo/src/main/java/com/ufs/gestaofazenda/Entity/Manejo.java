package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "Manejo")
@Table(name = "manejos")
public class Manejo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long manejoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_safra", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Safra safra;

    @Column(name = "operacao", nullable = false, length = 2000)
    public String operacao;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_responsavel", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Funcionario responsavel;
}
