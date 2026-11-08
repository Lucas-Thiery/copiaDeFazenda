package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Talhao")
@Table(name = "talhoes")
public class Talhao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long talhaoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "identificacao", nullable = false, length = 2000)
    public String identificacao;

    @Column(name = "area_hectares", nullable = false, precision = 19, scale = 4)
    public BigDecimal areaHectares;

    @Column(name = "localizacao", nullable = true, length = 2000)
    public String localizacao;

    @Column(name = "descricao_limites", nullable = true, length = 2000)
    public String descricaoLimites;

    @Column(name = "criado_em", nullable = true)
    public Instant dtaCadastro = Instant.now();
}
