package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class ProducaoColheitaDTO {
    @Data
    public static class ProducaoColheitaRequest {
        @NotNull public Long codSafra;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @PositiveOrZero public BigDecimal quantidade;

        @NotBlank public String unidade;

        public String justificativaPerda;

        public boolean parcial;
    }

    @Data
    public static class ProducaoColheitaResponse {
        public Long producaoColheitaId;
        public Long codFazenda;
        public Long codSafra;
        public LocalDate dtaOperacao;
        public BigDecimal quantidade;
        public String unidade;
        public String justificativaPerda;
        public boolean parcial;
    }
}
