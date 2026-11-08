package com.ufs.gestaofazenda.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.*;

public class RelatorioDTO {
    @Data
    public static class RelatorioResponse {
        public String tipo;
        public Long codFazenda;
        public LocalDate dtaInicio;
        public LocalDate dtaFim;
        public Map<String, Long> filtros = new LinkedHashMap<>();
        public List<String> colunas = new ArrayList<>();
        public List<List<String>> linhas = new ArrayList<>();
        public List<String> limitacoes = new ArrayList<>();
    }
}
