package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "ProducaoColheita")
@Table(name = "producoes_colheita")
public class ProducaoColheita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long producaoColheitaId;

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

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    public BigDecimal quantidade;

    @Column(name = "unidade", nullable = false, length = 2000)
    public String unidade;

    @Column(name = "justificativa_perda", nullable = true, length = 2000)
    public String justificativaPerda;

    @Column(name = "parcial", nullable = true)
    public boolean parcial = true;
}
