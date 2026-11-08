package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "PlanoManutencao")
@Table(name = "planos_manutencao")
public class PlanoManutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long planoManutencaoId;

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

    @Column(name = "servico", nullable = false, length = 2000)
    public String servico;

    @Column(name = "dta_prevista", nullable = true)
    public LocalDate dtaPrevista;

    @Column(name = "limite_medidor", nullable = true, precision = 19, scale = 4)
    public BigDecimal limiteMedidor;

    @Column(name = "concluido", nullable = true)
    public boolean concluido = false;
}
