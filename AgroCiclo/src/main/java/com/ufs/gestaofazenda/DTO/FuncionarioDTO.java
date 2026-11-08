package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class FuncionarioDTO {
    @Data
    public static class FuncionarioRequest {
        @NotNull public Long codCargo;

        @NotBlank public String nome;

        public String equipe;

        @NotBlank public String horarioTrabalho;

        @PositiveOrZero public BigDecimal custoHoraIndividual;
    }

    @Data
    public static class FuncionarioResponse {
        public Long funcionarioId;
        public Long codFazenda;
        public Long codCargo;
        public String nome;
        public String equipe;
        public String horarioTrabalho;
        public BigDecimal custoHoraIndividual;
        public BigDecimal custoHoraEfetivo;
        public boolean ativo;
    }
}
