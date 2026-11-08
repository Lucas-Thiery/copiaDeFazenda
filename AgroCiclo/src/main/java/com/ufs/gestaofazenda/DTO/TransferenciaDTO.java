package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.LocalDate;

public class TransferenciaDTO {
    @Data
    public static class TransferenciaRequest {
        @NotNull public Long codLoteDestino;
        @NotNull @PastOrPresent public LocalDate dtaOperacao;
    }
}
