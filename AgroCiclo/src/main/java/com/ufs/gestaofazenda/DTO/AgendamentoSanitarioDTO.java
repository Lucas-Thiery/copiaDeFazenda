package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class AgendamentoSanitarioDTO {
    @Data
    public static class AgendamentoSanitarioRequest {
        @NotNull public Long codCalendario;

        public Long codAnimal;

        public Long codLote;

        @NotNull public Long codResponsavel;

        @NotNull @FutureOrPresent public LocalDate dtaPrevista;
    }

    @Data
    public static class AgendamentoSanitarioResponse {
        public Long agendamentoSanitarioId;
        public Long codFazenda;
        public Long codCalendario;
        public Long codAnimal;
        public Long codLote;
        public Long codResponsavel;
        public LocalDate dtaPrevista;
        public boolean concluido;
    }
}
