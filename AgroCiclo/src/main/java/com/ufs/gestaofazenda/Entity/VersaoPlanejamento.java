package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "VersaoPlanejamento")
@Table(name = "versoes_planejamento")
public class VersaoPlanejamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long versaoPlanejamentoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_plano", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public PlanoManejo plano;

    @Column(name = "operacoes", nullable = false, length = 2000)
    public String operacoes;

    @Column(name = "dta_inicio", nullable = false)
    public LocalDate dtaInicio;

    @Column(name = "dta_fim", nullable = false)
    public LocalDate dtaFim;

    @Column(name = "versao", nullable = false)
    public Integer versao;
}
