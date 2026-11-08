package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class FazendaDTO {
    @Data
    public static class FazendaRequest {
        @NotBlank public String nome;

        @NotNull @Positive public BigDecimal areaHectares;

        @NotBlank public String localizacao;

        @NotBlank public String responsavel;
    }

    @Data
    public static class FazendaResponse {
        public Long fazendaId;
        public String nome;
        public BigDecimal areaHectares;
        public String localizacao;
        public String responsavel;
        public boolean ativa;
        public Instant dtaCadastro;
    }
}
