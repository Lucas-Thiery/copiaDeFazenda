package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.DTO.DashboardDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROPRIETARIO')")
public class DashboardService {
    private final RelatorioService relatorioService;
    private final FluxoCaixaService fluxoCaixaService;

    @Transactional(readOnly = true)
    public DashboardDTO.DashboardResponse consultar(LocalDate inicio, LocalDate fim) {
        var response = new DashboardDTO.DashboardResponse();
        response.setCustos(relatorioService.gerar("custos", inicio, fim, null, null, null));
        response.setProdutividade(
                relatorioService.gerar("produtividade", inicio, fim, null, null, null));
        response.setFluxoCaixa(fluxoCaixaService.consultar(inicio, fim));
        return response;
    }
}
