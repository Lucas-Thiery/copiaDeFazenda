package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class AbastecimentoDTO {
    @Data
    public static class AbastecimentoRequest {
        @NotNull public Long codMaquina;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @Positive public BigDecimal litros;

        @NotNull @Positive public BigDecimal custo;

        @PositiveOrZero public BigDecimal leitura;

        @NotBlank public String referencia;

        public Long codSafra;

        public Long codLote;

        public Long codTarefa;

        public Long codLancamento;
    }

    @Data
    public static class AbastecimentoResponse {
        public Long abastecimentoId;
        public Long codFazenda;
        public Long codMaquina;
        public LocalDate dtaOperacao;
        public BigDecimal litros;
        public BigDecimal custo;
        public BigDecimal leitura;
        public String referencia;
        public Long codSafra;
        public Long codLote;
        public Long codTarefa;
        public Long codLancamento;
    }
}
