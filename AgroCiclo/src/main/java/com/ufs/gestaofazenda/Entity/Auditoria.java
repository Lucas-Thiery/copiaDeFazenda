package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "auditorias")
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long auditoriaId;

    @Column(name = "cod_fazenda")
    public Long codFazenda;

    public String autor;
    public String tipoEvento;
    public String entidade;

    @Column(name = "registro_id")
    public Long codRegistro;

    @Column(name = "ocorrido_em")
    public Instant dtaOperacao = Instant.now();

    public String solicitacao;
}
