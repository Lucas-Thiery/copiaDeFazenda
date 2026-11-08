package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.FazendaDTO;
import com.ufs.gestaofazenda.Service.FazendaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/fazendas", "/fazendas", "/api/propriedades"})
@RequiredArgsConstructor
public class FazendaController {
    private final FazendaService fazendaService;

    @GetMapping
    public ResponseEntity<List<FazendaDTO.FazendaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(fazendaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FazendaDTO.FazendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fazendaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<FazendaDTO.FazendaResponse> cadastrar(
            @Valid @RequestBody FazendaDTO.FazendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fazendaService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<FazendaDTO.FazendaResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody FazendaDTO.FazendaRequest request) {
        return ResponseEntity.ok(fazendaService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fazendaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
