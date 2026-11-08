package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.UsoMaquinaDTO;
import com.ufs.gestaofazenda.Service.UsoMaquinaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/usos-maquina", "/usos-maquina"})
@RequiredArgsConstructor
public class UsoMaquinaController {
    private final UsoMaquinaService usoMaquinaService;

    @GetMapping
    public ResponseEntity<List<UsoMaquinaDTO.UsoMaquinaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(usoMaquinaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsoMaquinaDTO.UsoMaquinaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usoMaquinaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsoMaquinaDTO.UsoMaquinaResponse> cadastrar(
            @Valid @RequestBody UsoMaquinaDTO.UsoMaquinaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usoMaquinaService.cadastrar(request));
    }
}
