package com.ufs.gestaofazenda.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class CustoDTO {
    @Data
    public static class CustoResponse {
        public String destino;
        public Long codDestino;
        public LocalDate dtaInicio;
        public LocalDate dtaFim;
        public BigDecimal insumos;
        public BigDecimal maoObra;
        public BigDecimal despesas;
        public BigDecimal total;
        public BigDecimal denominador;
        public BigDecimal custoPorUnidade;
        public List<String> pendencias = new ArrayList<>();
    }
}
