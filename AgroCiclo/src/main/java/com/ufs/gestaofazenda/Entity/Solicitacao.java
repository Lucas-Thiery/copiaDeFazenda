package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(
        name = "solicitacoes",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"cod_fazenda", "cod_usuario", "chave"}))
public class Solicitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long solicitacaoId;

    @Column(name = "cod_fazenda", nullable = false)
    public Long codFazenda;

    @Column(name = "cod_usuario", nullable = false)
    public Long codUsuario;

    @Column(nullable = false, length = 200)
    public String chave;

    @Column(nullable = false, length = 64)
    public String impressao;

    @Column(nullable = false, length = 30)
    public String perfil;

    @Column(nullable = false)
    public int status;

    @Column(columnDefinition = "text")
    public String resposta;

    @Column(nullable = false)
    public Instant dtaOperacao = Instant.now();
}
