package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class PlanoManutencaoDTO {
    @Data
    public static class PlanoManutencaoRequest {
        @NotNull public Long codMaquina;

        @NotBlank public String servico;

        @FutureOrPresent public LocalDate dtaPrevista;

        @PositiveOrZero public BigDecimal limiteMedidor;
    }

    @Data
    public static class PlanoManutencaoResponse {
        public Long planoManutencaoId;
        public Long codFazenda;
        public Long codMaquina;
        public String servico;
        public LocalDate dtaPrevista;
        public BigDecimal limiteMedidor;
        public boolean concluido;
    }
}
