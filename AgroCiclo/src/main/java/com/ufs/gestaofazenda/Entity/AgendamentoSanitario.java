package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "AgendamentoSanitario")
@Table(name = "agendamentos_sanitarios")
public class AgendamentoSanitario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long agendamentoSanitarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_calendario", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public CalendarioSanitario calendario;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_responsavel", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Funcionario responsavel;

    @Column(name = "dta_prevista", nullable = false)
    public LocalDate dtaPrevista;

    @Column(name = "concluido", nullable = true)
    public boolean concluido = false;
}
