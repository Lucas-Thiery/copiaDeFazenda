package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class LoteDTO {
    @Data
    public static class LoteRequest {
        @NotNull public Long codPastagemAtual;

        @NotBlank public String identificacao;

        @NotNull @PastOrPresent public LocalDate dtaFormacao;
    }

    @Data
    public static class LoteResponse {
        public Long loteId;
        public Long codFazenda;
        public Long codPastagemAtual;
        public String identificacao;
        public LocalDate dtaFormacao;
        public boolean ativo;
    }
}
