package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class PartidaEstoqueDTO {
    @Data
    public static class PartidaEstoqueRequest {
        @NotNull public Long codItem;

        public Long codCompra;

        public String codigoLote;

        @NotNull @PastOrPresent public LocalDate dtaEntrada;

        public LocalDate dtaValidade;

        @NotNull @Positive public BigDecimal quantidadeInicial;

        @NotNull @PositiveOrZero public BigDecimal valorUnitarioOrigem;

        public String referenciaOrigem;

        @NotBlank public String unidade;
    }

    @Data
    public static class PartidaEstoqueResponse {
        public Long partidaEstoqueId;
        public Long codFazenda;
        public Long codItem;
        public Long codCompra;
        public String codigoLote;
        public LocalDate dtaEntrada;
        public LocalDate dtaValidade;
        public BigDecimal quantidadeInicial;
        public BigDecimal quantidadeAtual;
        public BigDecimal valorUnitarioOrigem;
        public String referenciaOrigem;
    }
}
