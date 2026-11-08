package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.CustoDTO;
import com.ufs.gestaofazenda.Service.CustoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/custos")
@RequiredArgsConstructor
public class CustoController {
    private final CustoService custoService;

    @GetMapping("/{destino}/{id}")
    public ResponseEntity<CustoDTO.CustoResponse> apurar(
            @PathVariable String destino,
            @PathVariable Long id,
            @RequestParam LocalDate dtaInicio,
            @RequestParam LocalDate dtaFim) {
        return ResponseEntity.ok(custoService.apurar(destino.toUpperCase(), id, dtaInicio, dtaFim));
    }
}
