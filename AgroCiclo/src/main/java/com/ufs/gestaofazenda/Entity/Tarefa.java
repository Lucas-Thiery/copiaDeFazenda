package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Tarefa")
@Table(name = "tarefas")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long tarefaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_executante", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Funcionario executante;

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

    @Column(name = "atividade", nullable = false, length = 2000)
    public String atividade;

    @Column(name = "local", nullable = false, length = 2000)
    public String local;

    @Column(name = "dta_inicio_previsto", nullable = false)
    public LocalDateTime dtaInicioPrevisto;

    @Column(name = "dta_fim_previsto", nullable = false)
    public LocalDateTime dtaFimPrevisto;

    @Column(name = "status", nullable = true, length = 2000)
    public String status = "PENDENTE";

    @Column(name = "dta_inicio_real", nullable = true)
    public LocalDateTime dtaInicioReal;

    @Column(name = "dta_fim_real", nullable = true)
    public LocalDateTime dtaFimReal;

    @Column(name = "taxa_preservada", nullable = true, precision = 19, scale = 4)
    public BigDecimal taxaPreservada;

    @Column(name = "duracao_horas", nullable = true, precision = 19, scale = 4)
    public BigDecimal duracaoHoras;

    @Column(name = "custo_mao_obra", nullable = true, precision = 19, scale = 4)
    public BigDecimal custoMaoObra;

    @Column(name = "observacoes", nullable = true, length = 2000)
    public String observacoes;
}
