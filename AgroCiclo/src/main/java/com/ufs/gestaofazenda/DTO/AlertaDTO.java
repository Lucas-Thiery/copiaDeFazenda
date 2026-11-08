package com.ufs.gestaofazenda.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.*;

public class AlertaDTO {
    @Data
    public static class AlertaResponse {
        public Long codRegistro;
        public String categoria;
        public String motivo;
        public LocalDate dtaReferencia;
    }

    @Data
    public static class EnvioResponse {
        public String resultado;
        public String mensagem;
    }
}
