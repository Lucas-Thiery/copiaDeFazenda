package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class TalhaoDTO {
    @Data
    public static class TalhaoRequest {
        @NotBlank public String identificacao;

        @NotNull @Positive public BigDecimal areaHectares;

        public String localizacao;

        public String descricaoLimites;
    }

    @Data
    public static class TalhaoResponse {
        public Long talhaoId;
        public Long codFazenda;
        public String identificacao;
        public BigDecimal areaHectares;
        public String localizacao;
        public String descricaoLimites;
        public Instant dtaCadastro;
    }
}
