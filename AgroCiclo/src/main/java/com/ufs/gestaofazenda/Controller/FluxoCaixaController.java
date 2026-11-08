package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.FluxoCaixaDTO;
import com.ufs.gestaofazenda.Service.FluxoCaixaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fluxo-caixa")
@RequiredArgsConstructor
public class FluxoCaixaController {
    private final FluxoCaixaService fluxoCaixaService;

    @GetMapping
    public ResponseEntity<FluxoCaixaDTO.FluxoCaixaResponse> consultar(
            @RequestParam LocalDate dtaInicio, @RequestParam LocalDate dtaFim) {
        return ResponseEntity.ok(fluxoCaixaService.consultar(dtaInicio, dtaFim));
    }
}
