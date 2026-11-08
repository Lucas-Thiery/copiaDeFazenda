package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class ManejoDTO {
    @Data
    public static class ManejoRequest {
        @NotNull public Long codSafra;

        @NotBlank public String operacao;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull public Long codResponsavel;
    }

    @Data
    public static class ManejoResponse {
        public Long manejoId;
        public Long codFazenda;
        public Long codSafra;
        public String operacao;
        public LocalDate dtaOperacao;
        public Long codResponsavel;
    }
}
