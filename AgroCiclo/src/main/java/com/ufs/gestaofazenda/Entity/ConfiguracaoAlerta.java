package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "ConfiguracaoAlerta")
@Table(name = "configuracoes_alerta")
public class ConfiguracaoAlerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long configuracaoAlertaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_fazenda", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Fazenda fazenda;

    @Column(name = "dias_vacina", nullable = false)
    public Integer diasVacina;

    @Column(name = "dias_validade", nullable = false)
    public Integer diasValidade;

    @Column(name = "dias_manutencao", nullable = false)
    public Integer diasManutencao;

    @Column(name = "margem_horas", nullable = false, precision = 19, scale = 4)
    public BigDecimal margemHoras;

    @Column(name = "margem_km", nullable = false, precision = 19, scale = 4)
    public BigDecimal margemKm;
}
