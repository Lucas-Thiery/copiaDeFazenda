package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class ManutencaoDTO {
    @Data
    public static class ManutencaoRequest {
        @NotNull public Long codMaquina;

        public Long codPlano;

        @NotBlank
        @Pattern(regexp = "PREVENTIVA|CORRETIVA")
        public String tipo;

        @NotBlank public String servico;

        public String pecas;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @PositiveOrZero public BigDecimal custo;

        @NotBlank public String referencia;

        public Long codSafra;

        public Long codLote;

        public Long codTarefa;

        public Long codLancamento;
    }

    @Data
    public static class ManutencaoResponse {
        public Long manutencaoId;
        public Long codFazenda;
        public Long codMaquina;
        public Long codPlano;
        public String tipo;
        public String servico;
        public String pecas;
        public LocalDate dtaOperacao;
        public BigDecimal custo;
        public String referencia;
        public Long codSafra;
        public Long codLote;
        public Long codTarefa;
        public Long codLancamento;
    }
}
