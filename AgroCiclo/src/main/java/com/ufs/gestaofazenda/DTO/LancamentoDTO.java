package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class LancamentoDTO {
    @Data
    public static class LancamentoRequest {
        @NotBlank
        @Pattern(regexp = "RECEITA|DESPESA")
        public String tipo;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotBlank public String descricao;

        @NotBlank public String categoria;

        @NotBlank public String referencia;

        @NotBlank public String emitente;

        @NotNull @Positive public BigDecimal valor;

        public Long codSafra;

        public Long codLote;

        public Long codTarefa;
    }

    @Data
    public static class LancamentoResponse {
        public Long lancamentoId;
        public Long codFazenda;
        public String tipo;
        public LocalDate dtaOperacao;
        public String descricao;
        public String categoria;
        public String referencia;
        public String emitente;
        public BigDecimal valor;
        public BigDecimal valorLiquidado;
        public Long codSafra;
        public Long codLote;
        public Long codTarefa;
        public boolean compraEstoque;
        public String avisoCarencia;
    }
}
