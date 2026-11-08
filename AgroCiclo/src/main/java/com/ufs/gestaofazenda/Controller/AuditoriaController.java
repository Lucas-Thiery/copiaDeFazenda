package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.AuditoriaDTO;
import com.ufs.gestaofazenda.Service.AuditoriaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AuditoriaDTO.AuditoriaResponse>> listar(
            @RequestParam LocalDate dtaInicio,
            @RequestParam LocalDate dtaFim,
            @RequestParam(required = false) String operacao) {
        return ResponseEntity.ok(auditoriaService.consultar(dtaInicio, dtaFim, operacao));
    }
}
