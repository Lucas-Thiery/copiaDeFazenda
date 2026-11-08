package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.PlanoManejoDTO;
import com.ufs.gestaofazenda.Service.PlanoManejoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/planos-manejo", "/planos-manejo"})
@RequiredArgsConstructor
public class PlanoManejoController {
    private final PlanoManejoService planoManejoService;

    @GetMapping
    public ResponseEntity<List<PlanoManejoDTO.PlanoManejoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(planoManejoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoManejoDTO.PlanoManejoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(planoManejoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PlanoManejoDTO.PlanoManejoResponse> cadastrar(
            @Valid @RequestBody PlanoManejoDTO.PlanoManejoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planoManejoService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<PlanoManejoDTO.PlanoManejoResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody PlanoManejoDTO.PlanoManejoRequest request) {
        return ResponseEntity.ok(planoManejoService.atualizar(id, request));
    }
}
