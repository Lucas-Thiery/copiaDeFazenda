package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.VersaoPlanejamentoDTO;
import com.ufs.gestaofazenda.Service.VersaoPlanejamentoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/versoes-planejamento", "/versoes-planejamento"})
@RequiredArgsConstructor
public class VersaoPlanejamentoController {
    private final VersaoPlanejamentoService versaoPlanejamentoService;

    @GetMapping
    public ResponseEntity<List<VersaoPlanejamentoDTO.VersaoPlanejamentoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(versaoPlanejamentoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersaoPlanejamentoDTO.VersaoPlanejamentoResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(versaoPlanejamentoService.buscarPorId(id));
    }
}
