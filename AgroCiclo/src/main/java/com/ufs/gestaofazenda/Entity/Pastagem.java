package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "Pastagem")
@Table(name = "pastagens")
public class Pastagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long pastagemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "identificacao", nullable = false, length = 2000)
    public String identificacao;

    @Column(name = "descricao", nullable = false, length = 2000)
    public String descricao;

    @Column(name = "ativa", nullable = true)
    public boolean ativa = true;
}
