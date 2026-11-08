package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class PlanoManejoDTO {
    @Data
    public static class PlanoManejoRequest {
        @NotNull public Long codSafra;

        @NotBlank public String operacoes;

        @NotNull public LocalDate dtaInicio;

        @NotNull public LocalDate dtaFim;
    }

    @Data
    public static class PlanoManejoResponse {
        public Long planoManejoId;
        public Long codFazenda;
        public Long codSafra;
        public String operacoes;
        public LocalDate dtaInicio;
        public LocalDate dtaFim;
        public Integer versao;
    }
}
