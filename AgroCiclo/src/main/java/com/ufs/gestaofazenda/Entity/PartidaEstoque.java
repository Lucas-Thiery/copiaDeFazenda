package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "PartidaEstoque")
@Table(name = "partidas_estoque")
public class PartidaEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long partidaEstoqueId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_item_estoque", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public ItemEstoque item;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_compra", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Compra compra;

    @Column(name = "codigo_lote", nullable = true, length = 2000)
    public String codigoLote;

    @Column(name = "data_entrada", nullable = false)
    public LocalDate dtaEntrada;

    @Column(name = "data_validade", nullable = true)
    public LocalDate dtaValidade;

    @Column(name = "quantidade_inicial", nullable = false, precision = 19, scale = 4)
    public BigDecimal quantidadeInicial;

    @Column(name = "quantidade_atual", nullable = true, precision = 19, scale = 4)
    public BigDecimal quantidadeAtual;

    @Column(name = "valor_unitario_origem", nullable = false, precision = 19, scale = 4)
    public BigDecimal valorUnitarioOrigem;

    @Column(name = "referencia_origem", nullable = true, length = 2000)
    public String referenciaOrigem;
}
