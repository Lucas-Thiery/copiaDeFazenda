package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;

public class SetupDTO {
    @Data
    public static class SetupRequest {
        @NotBlank public String propriedadeNome;
        @NotNull @Positive public BigDecimal areaHectares;
        @NotBlank public String localizacao;
        @NotBlank public String responsavel;
        @NotBlank public String nomeProprietario;
        @NotBlank public String login;

        @NotBlank
        @Size(min = 10, max = 72)
        public String senha;
    }
}
