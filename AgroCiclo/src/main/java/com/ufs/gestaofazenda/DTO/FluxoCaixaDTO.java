package com.ufs.gestaofazenda.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FluxoCaixaDTO {
    @Data
    public static class FluxoCaixaResponse {
        public LocalDate dtaInicio;
        public LocalDate dtaFim;
        public BigDecimal saldoInicialRegistrado;
        public BigDecimal recebimentos;
        public BigDecimal pagamentos;
        public BigDecimal saldoPeriodo;
        public BigDecimal saldoFinalRegistrado;
        public BigDecimal receitasPendentes;
        public BigDecimal despesasPendentes;
        public String criterio;
    }
}
