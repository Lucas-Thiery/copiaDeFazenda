package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class ProducaoPecuariaDTO {
    @Data
    public static class ProducaoPecuariaRequest {
        public Long codAnimal;

        public Long codLote;

        @NotBlank public String produto;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @PositiveOrZero public BigDecimal quantidade;

        @NotBlank public String unidade;

        public String destinacao;
    }

    @Data
    public static class ProducaoPecuariaResponse {
        public Long producaoPecuariaId;
        public Long codFazenda;
        public Long codAnimal;
        public Long codLote;
        public String produto;
        public LocalDate dtaOperacao;
        public BigDecimal quantidade;
        public String unidade;
        public String destinacao;
        public String avisoCarencia;
    }
}
