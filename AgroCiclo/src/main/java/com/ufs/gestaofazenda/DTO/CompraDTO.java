package com.ufs.gestaofazenda.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class CompraDTO {
    @Data
    public static class CompraRequest {
        @NotNull public Long codFornecedor;

        @NotBlank public String referencia;

        @NotNull @PastOrPresent public LocalDate dtaOperacao;

        @NotNull @Positive public BigDecimal total;

        @NotEmpty @Valid public List<ItemCompraDTO.ItemCompraRequest> itens;
    }

    @Data
    public static class CompraResponse {
        public Long compraId;
        public Long codFazenda;
        public Long codFornecedor;
        public String referencia;
        public LocalDate dtaOperacao;
        public BigDecimal total;
    }
}
