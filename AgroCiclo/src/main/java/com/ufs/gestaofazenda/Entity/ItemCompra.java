package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "ItemCompra")
@Table(name = "itens_compra")
public class ItemCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long itemCompraId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_compra", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_item", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public ItemEstoque item;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 4)
    public BigDecimal quantidade;

    @Column(name = "unidade", nullable = false, length = 2000)
    public String unidade;

    @Column(name = "valor_unitario", nullable = false, precision = 19, scale = 4)
    public BigDecimal valorUnitario;

    @Column(name = "codigo_lote", nullable = true, length = 2000)
    public String codigoLote;

    @Column(name = "dta_validade", nullable = true)
    public LocalDate dtaValidade;
}
