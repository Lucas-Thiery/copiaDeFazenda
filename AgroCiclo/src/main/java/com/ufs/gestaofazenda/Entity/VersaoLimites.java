package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "VersaoLimites")
@Table(name = "versoes_limites")
public class VersaoLimites {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long versaoLimitesId;

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

    @Column(name = "descricao_limites", nullable = true, length = 2000)
    public String descricaoLimites;

    @Column(name = "dta_registro", nullable = true)
    public Instant dtaRegistro = Instant.now();
}
