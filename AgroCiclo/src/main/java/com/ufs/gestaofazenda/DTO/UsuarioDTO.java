package com.ufs.gestaofazenda.DTO;

import com.ufs.gestaofazenda.Entity.Perfil;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class UsuarioDTO {
    @Data
    public static class UsuarioRequest {
        @Email public String email;
        @NotNull public Long codFuncionario;

        @NotBlank public String login;

        @NotNull public Perfil perfil;

        @NotBlank
        @Size(min = 10, max = 72)
        public String senha;

        @NotEmpty public Set<@NotNull Long> codFazendas;
    }

    @Data
    public static class UsuarioResponse {
        public String email;
        public Long usuarioId;
        public Long codFuncionario;
        public String login;
        public Perfil perfil;
        public boolean ativo;
        public Instant dtaCadastro;
        public Set<Long> codFazendas;
    }

    @Data
    public static class EmailRequest {
        @NotBlank @Email public String email;
    }
}
