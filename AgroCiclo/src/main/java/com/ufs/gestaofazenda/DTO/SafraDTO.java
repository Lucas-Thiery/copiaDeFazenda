package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class SafraDTO {
    @Data
    public static class SafraRequest {
        @NotNull public Long codTalhao;

        @NotBlank public String identificacao;

        @NotBlank public String cultura;

        public String variedade;

        @NotNull public LocalDate dtaInicio;

        public LocalDate dtaFimPrevista;

        @NotNull @Positive public BigDecimal areaPlantadaHa;

        public String observacoes;
    }

    @Data
    public static class SafraResponse {
        public Long safraId;
        public Long codFazenda;
        public Long codTalhao;
        public String identificacao;
        public String cultura;
        public String variedade;
        public LocalDate dtaInicio;
        public LocalDate dtaFimPrevista;
        public BigDecimal areaPlantadaHa;
        public String status;
        public String observacoes;
    }
}
