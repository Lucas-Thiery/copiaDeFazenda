package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class MovimentacaoPastagemDTO {
    @Data
    public static class MovimentacaoPastagemRequest {
        @NotNull public Long codLote;

        public Long codOrigem;

        @NotNull public Long codDestino;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;
    }

    @Data
    public static class MovimentacaoPastagemResponse {
        public Long movimentacaoPastagemId;
        public Long codFazenda;
        public Long codLote;
        public Long codOrigem;
        public Long codDestino;
        public LocalDate dtaOperacao;
    }
}
