package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class CalendarioSanitarioDTO {
    @Data
    public static class CalendarioSanitarioRequest {
        public String especie;

        public Long codLote;

        @NotNull public Long codProduto;

        @NotBlank public String acao;

        @NotBlank public String orientacaoTecnica;

        public LocalDate dtaPrevista;

        @Positive public Integer intervaloDias;
    }

    @Data
    public static class CalendarioSanitarioResponse {
        public Long calendarioSanitarioId;
        public Long codFazenda;
        public String especie;
        public Long codLote;
        public Long codProduto;
        public String acao;
        public String orientacaoTecnica;
        public LocalDate dtaPrevista;
        public Integer intervaloDias;
    }
}
