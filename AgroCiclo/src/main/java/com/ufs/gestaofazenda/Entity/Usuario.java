package com.ufs.gestaofazenda.Entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.*;
import java.util.*;

@Data
@Entity(name = "Usuario")
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_funcionario", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Funcionario funcionario;

    @Column(name = "login", nullable = false, length = 254)
    public String login;

    @Column(name = "senha_hash", nullable = false, length = 2000)
    public String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public Perfil perfil;

    @Column(name = "ativo", nullable = true)
    public boolean ativo = true;

    @Column(name = "criado_em", nullable = true)
    public Instant dtaCadastro = Instant.now();

    @ManyToMany
    @JoinTable(
            name = "usuarios_fazendas",
            joinColumns = @JoinColumn(name = "cod_usuario"),
            inverseJoinColumns = @JoinColumn(name = "cod_fazenda"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public Set<Fazenda> fazendas = new HashSet<>();

    @Column(length = 254)
    public String email;

    @Column(name = "nome_proprietario", length = 140)
    private String nomeProprietario;

    public boolean primeiraFazendaPendente() {
        return perfil == Perfil.PROPRIETARIO && funcionario == null;
    }

    public boolean podeAutenticar() {
        return ativo && (funcionario != null ? funcionario.isAtivo() : primeiraFazendaPendente());
    }
}
