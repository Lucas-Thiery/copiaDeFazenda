package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class MovimentacaoEstoqueDTO {
    @Data
    public static class MovimentacaoEstoqueRequest {
        @NotNull public Long codPartida;

        @NotBlank
        @Pattern(regexp = "ENTRADA|USO|DESCARTE")
        public String tipo;

        @NotNull @Positive public BigDecimal quantidade;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotBlank public String motivo;

        public Long codSafra;

        public Long codLote;

        public Long codTarefa;

        public String referenciaOrigem;

        public Long codResponsavel;

        @NotBlank public String unidade;
    }

    @Data
    public static class MovimentacaoEstoqueResponse {
        public Long movimentacaoEstoqueId;
        public Long codFazenda;
        public Long codPartida;
        public String tipo;
        public BigDecimal quantidade;
        public LocalDate dtaOperacao;
        public String motivo;
        public BigDecimal valorTotalOrigem;
        public Long codSafra;
        public Long codLote;
        public Long codTarefa;
        public String referenciaOrigem;
        public Long codResponsavel;
    }
}
