package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "EtapaCultivo")
@Table(name = "etapas_cultivo")
public class EtapaCultivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long etapaCultivoId;

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

    @Column(name = "etapa", nullable = false, length = 2000)
    public String etapa;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "observacoes", nullable = true, length = 2000)
    public String observacoes;
}
