package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.AbastecimentoDTO;
import com.ufs.gestaofazenda.Service.AbastecimentoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/abastecimentos", "/abastecimentos"})
@RequiredArgsConstructor
public class AbastecimentoController {
    private final AbastecimentoService abastecimentoService;

    @GetMapping
    public ResponseEntity<List<AbastecimentoDTO.AbastecimentoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(abastecimentoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbastecimentoDTO.AbastecimentoResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(abastecimentoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AbastecimentoDTO.AbastecimentoResponse> cadastrar(
            @Valid @RequestBody AbastecimentoDTO.AbastecimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(abastecimentoService.cadastrar(request));
    }
}
