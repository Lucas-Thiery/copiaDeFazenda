package com.ufs.gestaofazenda.DTO;

import lombok.Data;

public class DashboardDTO {
    @Data
    public static class DashboardResponse {
        public RelatorioDTO.RelatorioResponse custos;
        public RelatorioDTO.RelatorioResponse produtividade;
        public FluxoCaixaDTO.FluxoCaixaResponse fluxoCaixa;
    }
}
