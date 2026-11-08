package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Cargo")
@Table(name = "cargos")
public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long cargoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "nome", nullable = false, length = 2000)
    public String nome;

    @Column(name = "atribuicoes", nullable = false, length = 2000)
    public String atribuicoes;

    @Column(name = "custo_hora_referencia", nullable = false, precision = 19, scale = 4)
    public BigDecimal custoHoraReferencia;
}
