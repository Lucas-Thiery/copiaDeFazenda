package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class TarefaDTO {
    @Data
    public static class TarefaRequest {
        @NotNull public Long codExecutante;

        public Long codSafra;

        public Long codLote;

        @NotBlank public String atividade;

        @NotBlank public String local;

        @NotNull public LocalDateTime dtaInicioPrevisto;

        @NotNull public LocalDateTime dtaFimPrevisto;

        public String observacoes;
    }

    @Data
    public static class TarefaResponse {
        public Long tarefaId;
        public Long codFazenda;
        public Long codExecutante;
        public Long codSafra;
        public Long codLote;
        public String atividade;
        public String local;
        public LocalDateTime dtaInicioPrevisto;
        public LocalDateTime dtaFimPrevisto;
        public String status;
        public LocalDateTime dtaInicioReal;
        public LocalDateTime dtaFimReal;
        public BigDecimal taxaPreservada;
        public BigDecimal duracaoHoras;
        public BigDecimal custoMaoObra;
        public String observacoes;
    }
}
