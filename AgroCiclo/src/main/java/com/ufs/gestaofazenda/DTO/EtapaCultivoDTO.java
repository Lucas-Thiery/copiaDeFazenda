package com.ufs.gestaofazenda.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class EtapaCultivoDTO {
    @Data
    public static class EtapaCultivoRequest {
        public Long codSafra;

        @Valid public SafraDTO.SafraRequest novaSafra;

        @NotBlank
        @Pattern(regexp = "PREPARO|PLANTIO|COLHEITA|ENCERRAMENTO")
        public String etapa;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        public String observacoes;
    }

    @Data
    public static class EtapaCultivoResponse {
        public Long etapaCultivoId;
        public Long codFazenda;
        public Long codSafra;
        public String etapa;
        public LocalDate dtaOperacao;
        public String observacoes;
    }
}
