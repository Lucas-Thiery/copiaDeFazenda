package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "ItemEstoque")
@Table(name = "itens_estoque")
public class ItemEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long itemEstoqueId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "codigo", nullable = true, length = 2000)
    public String codigo;

    @Column(name = "nome", nullable = false, length = 2000)
    public String nome;

    @Column(name = "categoria", nullable = false, length = 2000)
    public String categoria;

    @Column(name = "unidade_medida", nullable = false, length = 2000)
    public String unidadeMedida;

    @Column(name = "estoque_minimo", nullable = false, precision = 19, scale = 4)
    public BigDecimal estoqueMinimo;

    @Column(name = "ativo", nullable = true)
    public boolean ativo = true;
}
