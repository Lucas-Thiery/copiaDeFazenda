package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class LiquidacaoDTO {
    @Data
    public static class LiquidacaoRequest {
        @NotNull public Long codLancamento;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @Positive public BigDecimal valor;

        @NotBlank public String comprovante;
    }

    @Data
    public static class LiquidacaoResponse {
        public Long liquidacaoId;
        public Long codFazenda;
        public Long codLancamento;
        public LocalDate dtaOperacao;
        public BigDecimal valor;
        public String comprovante;
    }
}
