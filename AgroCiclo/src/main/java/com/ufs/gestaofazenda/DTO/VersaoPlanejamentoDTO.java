package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class VersaoPlanejamentoDTO {
    @Data
    public static class VersaoPlanejamentoRequest {
        @NotNull public Long codPlano;

        @NotBlank public String operacoes;

        @NotNull public LocalDate dtaInicio;

        @NotNull public LocalDate dtaFim;
    }

    @Data
    public static class VersaoPlanejamentoResponse {
        public Long versaoPlanejamentoId;
        public Long codFazenda;
        public Long codPlano;
        public String operacoes;
        public LocalDate dtaInicio;
        public LocalDate dtaFim;
        public Integer versao;
    }
}
