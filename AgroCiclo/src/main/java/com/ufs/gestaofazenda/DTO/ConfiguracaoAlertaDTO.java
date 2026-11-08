package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class ConfiguracaoAlertaDTO {
    @Data
    public static class ConfiguracaoAlertaRequest {
        @NotNull @PositiveOrZero public Integer diasVacina;

        @NotNull @PositiveOrZero public Integer diasValidade;

        @NotNull @PositiveOrZero public Integer diasManutencao;

        @NotNull @PositiveOrZero public BigDecimal margemHoras;

        @NotNull @PositiveOrZero public BigDecimal margemKm;
    }

    @Data
    public static class ConfiguracaoAlertaResponse {
        public Long configuracaoAlertaId;
        public Long codFazenda;
        public Integer diasVacina;
        public Integer diasValidade;
        public Integer diasManutencao;
        public BigDecimal margemHoras;
        public BigDecimal margemKm;
    }
}
