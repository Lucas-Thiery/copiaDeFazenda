package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Maquina")
@Table(name = "maquinas")
public class Maquina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long maquinaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "identificacao", nullable = false, length = 2000)
    public String identificacao;

    @Column(name = "modelo", nullable = false, length = 2000)
    public String modelo;

    @Column(name = "ano", nullable = false)
    public Integer ano;

    @Column(name = "chassi", nullable = true, length = 2000)
    public String chassi;

    @Column(name = "abastecivel", nullable = true)
    public boolean abastecivel = false;

    @Column(name = "unidade_medidor", nullable = true, length = 2000)
    public String unidadeMedidor;

    @Column(name = "leitura", nullable = true, precision = 19, scale = 4)
    public BigDecimal leitura = BigDecimal.ZERO;

    @Column(name = "dta_leitura", nullable = true)
    public LocalDate dtaLeitura;
}
