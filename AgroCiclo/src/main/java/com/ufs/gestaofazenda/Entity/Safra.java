package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Safra")
@Table(name = "safras")
public class Safra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long safraId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_talhao", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Talhao talhao;

    @Column(name = "identificacao", nullable = false, length = 2000)
    public String identificacao;

    @Column(name = "cultura", nullable = false, length = 2000)
    public String cultura;

    @Column(name = "variedade", nullable = true, length = 2000)
    public String variedade;

    @Column(name = "data_inicio", nullable = false)
    public LocalDate dtaInicio;

    @Column(name = "data_fim_prevista", nullable = true)
    public LocalDate dtaFimPrevista;

    @Column(name = "area_plantada_ha", nullable = false, precision = 19, scale = 4)
    public BigDecimal areaPlantadaHa;

    @Column(name = "status", nullable = true, length = 2000)
    public String status = "PLANEJADA";

    @Column(name = "observacoes", nullable = true, length = 2000)
    public String observacoes;
}
