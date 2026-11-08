package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class ItemEstoqueDTO {
    @Data
    public static class ItemEstoqueRequest {
        public String codigo;

        @NotBlank public String nome;

        @NotBlank
        @Pattern(regexp = "INSUMO|DEFENSIVO|RACAO|VACINA|MEDICAMENTO")
        public String categoria;

        @NotBlank public String unidadeMedida;

        @NotNull @PositiveOrZero public BigDecimal estoqueMinimo;
    }

    @Data
    public static class ItemEstoqueResponse {
        public Long itemEstoqueId;
        public Long codFazenda;
        public String codigo;
        public String nome;
        public String categoria;
        public String unidadeMedida;
        public BigDecimal estoqueMinimo;
        public boolean ativo;
        public BigDecimal saldo;
        public boolean abaixoMinimo;
    }
}
