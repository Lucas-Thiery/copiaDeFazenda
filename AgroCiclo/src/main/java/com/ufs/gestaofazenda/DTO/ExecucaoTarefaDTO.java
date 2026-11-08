package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.LocalDateTime;

public class ExecucaoTarefaDTO {
    @Data
    public static class ExecucaoTarefaRequest {
        @NotNull @PastOrPresent public LocalDateTime dtaInicio;
        @NotNull @PastOrPresent public LocalDateTime dtaFim;
        public String observacoes;
    }
}
