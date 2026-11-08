package com.ufs.gestaofazenda.DTO;

import lombok.Data;

import java.time.Instant;

public class AuditoriaDTO {
    @Data
    public static class AuditoriaResponse {
        public Long auditoriaId;
        public Long codFazenda;
        public String autor;
        public String operacao;
        public String entidade;
        public Long codRegistro;
        public Instant dtaOperacao;
        public String solicitacao;
    }
}
