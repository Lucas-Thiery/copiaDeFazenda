package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.TalhaoDTO;
import com.ufs.gestaofazenda.Service.TalhaoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/talhoes", "/talhoes"})
@RequiredArgsConstructor
public class TalhaoController {
    private final TalhaoService talhaoService;

    @GetMapping
    public ResponseEntity<List<TalhaoDTO.TalhaoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(talhaoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TalhaoDTO.TalhaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(talhaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TalhaoDTO.TalhaoResponse> cadastrar(
            @Valid @RequestBody TalhaoDTO.TalhaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(talhaoService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<TalhaoDTO.TalhaoResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody TalhaoDTO.TalhaoRequest request) {
        return ResponseEntity.ok(talhaoService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        talhaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
