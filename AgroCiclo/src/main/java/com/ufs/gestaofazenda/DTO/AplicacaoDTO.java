package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class AplicacaoDTO {
    @Data
    public static class AplicacaoRequest {
        @NotNull public Long codPartida;

        public Long codSafra;

        public Long codAnimal;

        public Long codLote;

        public Long codTarefa;

        public Long codManejo;

        public Long codAgendamento;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @Positive public BigDecimal quantidade;

        @NotBlank public String unidade;

        @NotNull public Long codResponsavel;

        public String dosagem;

        public String orientacaoTecnica;

        @PositiveOrZero public Integer carenciaDias;

        @NotBlank public String referencia;
    }

    @Data
    public static class AplicacaoResponse {
        public Long aplicacaoId;
        public Long codFazenda;
        public Long codPartida;
        public Long codSafra;
        public Long codAnimal;
        public Long codLote;
        public Long codTarefa;
        public Long codManejo;
        public Long codAgendamento;
        public LocalDate dtaOperacao;
        public BigDecimal quantidade;
        public String unidade;
        public Long codResponsavel;
        public String dosagem;
        public String orientacaoTecnica;
        public Integer carenciaDias;
        public BigDecimal custo;
        public String referencia;
    }
}
