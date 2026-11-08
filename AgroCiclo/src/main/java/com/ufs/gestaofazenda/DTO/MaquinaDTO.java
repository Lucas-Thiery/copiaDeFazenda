package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class MaquinaDTO {
    @Data
    public static class MaquinaRequest {
        @NotBlank public String identificacao;

        @NotBlank public String modelo;

        @NotNull
        @Min(1900)
        public Integer ano;

        public String chassi;

        public boolean abastecivel;

        @Pattern(regexp = "HORAS|KM")
        public String unidadeMedidor;

        @PositiveOrZero public BigDecimal leitura;

        public LocalDate dtaLeitura;
    }

    @Data
    public static class MaquinaResponse {
        public Long maquinaId;
        public Long codFazenda;
        public String identificacao;
        public String modelo;
        public Integer ano;
        public String chassi;
        public boolean abastecivel;
        public String unidadeMedidor;
        public BigDecimal leitura;
        public LocalDate dtaLeitura;
    }
}
