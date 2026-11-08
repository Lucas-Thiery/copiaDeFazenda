package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class FornecedorDTO {
    @Data
    public static class FornecedorRequest {
        @NotBlank public String identificacao;

        @NotBlank public String contato;
    }

    @Data
    public static class FornecedorResponse {
        public Long fornecedorId;
        public Long codFazenda;
        public String identificacao;
        public String contato;
    }
}
