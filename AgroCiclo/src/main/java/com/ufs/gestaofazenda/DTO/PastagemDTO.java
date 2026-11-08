package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class PastagemDTO {
    @Data
    public static class PastagemRequest {
        @NotBlank public String identificacao;

        @NotBlank public String descricao;
    }

    @Data
    public static class PastagemResponse {
        public Long pastagemId;
        public Long codFazenda;
        public String identificacao;
        public String descricao;
        public boolean ativa;
    }
}
