package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class AnimalDTO {
    @Data
    public static class AnimalRequest {
        @NotNull public Long codLoteAtual;

        @NotBlank public String identificacao;

        @NotBlank public String especie;

        @NotBlank public String raca;

        @NotNull @Positive public BigDecimal peso;

        @PositiveOrZero public Integer idadeMeses;

        @PastOrPresent public LocalDate dtaNascimento;

        @NotNull @PastOrPresent public LocalDate dtaEntrada;

        public String observacoes;
    }

    @Data
    public static class AnimalResponse {
        public Long animalId;
        public Long codFazenda;
        public Long codLoteAtual;
        public String identificacao;
        public String especie;
        public String raca;
        public BigDecimal peso;
        public Integer idadeMeses;
        public LocalDate dtaNascimento;
        public LocalDate dtaEntrada;
        public String observacoes;
        public boolean ativo;
    }
}
