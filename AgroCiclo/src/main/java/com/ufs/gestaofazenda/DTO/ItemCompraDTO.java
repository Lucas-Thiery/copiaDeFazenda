package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class ItemCompraDTO {
    @Data
    public static class ItemCompraRequest {
        public Long codCompra;

        @NotNull public Long codItem;

        @NotNull @Positive public BigDecimal quantidade;

        @NotBlank public String unidade;

        @NotNull @Positive public BigDecimal valorUnitario;

        public String codigoLote;

        public LocalDate dtaValidade;
    }

    @Data
    public static class ItemCompraResponse {
        public Long itemCompraId;
        public Long codFazenda;
        public Long codCompra;
        public Long codItem;
        public BigDecimal quantidade;
        public String unidade;
        public BigDecimal valorUnitario;
        public String codigoLote;
        public LocalDate dtaValidade;
    }
}
