package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class UsoMaquinaDTO {
    @Data
    public static class UsoMaquinaRequest {
        @NotNull public Long codMaquina;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @PositiveOrZero public BigDecimal leitura;

        public Long codSafra;

        public Long codLote;

        public Long codTarefa;
    }

    @Data
    public static class UsoMaquinaResponse {
        public Long usoMaquinaId;
        public Long codFazenda;
        public Long codMaquina;
        public LocalDate dtaOperacao;
        public BigDecimal leitura;
        public BigDecimal intervaloUso;
        public Long codSafra;
        public Long codLote;
        public Long codTarefa;
    }
}
