package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class VinculoAnimalLoteDTO {
    @Data
    public static class VinculoAnimalLoteRequest {
        @NotNull public Long codAnimal;

        @NotNull public Long codLote;

        @NotNull public LocalDate dtaInicio;

        public LocalDate dtaFim;
    }

    @Data
    public static class VinculoAnimalLoteResponse {
        public Long vinculoAnimalLoteId;
        public Long codFazenda;
        public Long codAnimal;
        public Long codLote;
        public LocalDate dtaInicio;
        public LocalDate dtaFim;
    }
}
