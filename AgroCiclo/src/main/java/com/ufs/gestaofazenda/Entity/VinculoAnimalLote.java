package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "VinculoAnimalLote")
@Table(name = "vinculos_animal_lote")
public class VinculoAnimalLote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long vinculoAnimalLoteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_animal", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_lote", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lote lote;

    @Column(name = "data_inicio", nullable = false)
    public LocalDate dtaInicio;

    @Column(name = "data_fim", nullable = true)
    public LocalDate dtaFim;
}
