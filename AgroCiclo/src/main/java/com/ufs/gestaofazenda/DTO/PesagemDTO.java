package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class PesagemDTO {
    @Data
    public static class PesagemRequest {
        public Long codAnimal;

        public Long codLote;

        @NotNull @PastOrPresent public LocalDate dtaPesagem;

        @NotNull @Positive public BigDecimal peso;

        public String observacao;
    }

    @Data
    public static class PesagemResponse {
        public Long pesagemId;
        public Long codFazenda;
        public Long codAnimal;
        public Long codLote;
        public LocalDate dtaPesagem;
        public BigDecimal peso;
        public String composicao;
        public String observacao;
    }
}
