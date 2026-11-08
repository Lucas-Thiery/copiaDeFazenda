package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.PlanoManutencaoDTO;
import com.ufs.gestaofazenda.Service.PlanoManutencaoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/planos-manutencao", "/planos-manutencao"})
@RequiredArgsConstructor
public class PlanoManutencaoController {
    private final PlanoManutencaoService planoManutencaoService;

    @GetMapping
    public ResponseEntity<List<PlanoManutencaoDTO.PlanoManutencaoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(planoManutencaoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoManutencaoDTO.PlanoManutencaoResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(planoManutencaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PlanoManutencaoDTO.PlanoManutencaoResponse> cadastrar(
            @Valid @RequestBody PlanoManutencaoDTO.PlanoManutencaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planoManutencaoService.cadastrar(request));
    }
}
