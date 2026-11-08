package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
@Entity(name = "Fazenda")
@Table(name = "propriedades")
public class Fazenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long fazendaId;

    @Column(name = "nome", nullable = false, length = 2000)
    public String nome;

    @Column(name = "area_hectares", nullable = false, precision = 19, scale = 4)
    public BigDecimal areaHectares;

    @Column(name = "localizacao", nullable = false, length = 2000)
    public String localizacao;

    @Column(name = "responsavel", nullable = false, length = 2000)
    public String responsavel;

    @Column(name = "ativa", nullable = true)
    public boolean ativa = true;

    @Column(name = "criada_em", nullable = true)
    public Instant dtaCadastro = Instant.now();
}
