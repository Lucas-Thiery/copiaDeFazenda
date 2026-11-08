package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Lancamento")
@Table(name = "lancamentos")
public class Lancamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long lancamentoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "tipo", nullable = false, length = 2000)
    public String tipo;

    @Column(name = "dta_operacao", nullable = false)
    public LocalDate dtaOperacao;

    @Column(name = "descricao", nullable = false, length = 2000)
    public String descricao;

    @Column(name = "categoria", nullable = false, length = 2000)
    public String categoria;

    @Column(name = "referencia", nullable = false, length = 2000)
    public String referencia;

    @Column(name = "emitente", nullable = false, length = 2000)
    public String emitente;

    @Column(name = "valor", nullable = false, precision = 19, scale = 4)
    public BigDecimal valor;

    @Column(name = "valor_liquidado", nullable = true, precision = 19, scale = 4)
    public BigDecimal valorLiquidado = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_safra", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Safra safra;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_lote", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cod_tarefa", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Tarefa tarefa;

    @Column(name = "compra_estoque", nullable = true)
    public boolean compraEstoque = false;

    @Column(name = "aviso_carencia", nullable = true, length = 2000)
    public String avisoCarencia;
}
