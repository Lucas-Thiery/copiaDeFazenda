package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.AlertaDTO;
import com.ufs.gestaofazenda.Service.AlertaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {
    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaDTO.AlertaResponse>> consultar(
            @RequestParam(required = false) LocalDate dtaReferencia,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(
                alertaService.consultar(
                        dtaReferencia == null ? LocalDate.now() : dtaReferencia, categoria));
    }

    @PostMapping("/enviar")
    public ResponseEntity<AlertaDTO.EnvioResponse> enviar() {
        return ResponseEntity.ok(alertaService.enviar());
    }
}
