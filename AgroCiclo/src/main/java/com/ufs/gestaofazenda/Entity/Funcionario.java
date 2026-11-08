package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Funcionario")
@Table(name = "funcionarios")
public class Funcionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long funcionarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_cargo", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Cargo cargo;

    @Column(name = "nome", nullable = false, length = 2000)
    public String nome;

    @Column(name = "equipe", nullable = true, length = 2000)
    public String equipe;

    @Column(name = "horario_trabalho", nullable = false, length = 2000)
    public String horarioTrabalho;

    @Column(name = "custo_hora_individual", nullable = true, precision = 19, scale = 4)
    public BigDecimal custoHoraIndividual;

    @Column(name = "custo_hora_efetivo", nullable = true, precision = 19, scale = 4)
    public BigDecimal custoHoraEfetivo;

    @Column(name = "ativo", nullable = true)
    public boolean ativo = true;
}
