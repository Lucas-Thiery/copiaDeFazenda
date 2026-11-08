package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class CargoDTO {
    @Data
    public static class CargoRequest {
        @NotBlank public String nome;

        @NotBlank public String atribuicoes;

        @NotNull @PositiveOrZero public BigDecimal custoHoraReferencia;
    }

    @Data
    public static class CargoResponse {
        public Long cargoId;
        public Long codFazenda;
        public String nome;
        public String atribuicoes;
        public BigDecimal custoHoraReferencia;
    }
}
