package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.PastagemDTO;
import com.ufs.gestaofazenda.Service.PastagemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/pastagens", "/pastagens"})
@RequiredArgsConstructor
public class PastagemController {
    private final PastagemService pastagemService;

    @GetMapping
    public ResponseEntity<List<PastagemDTO.PastagemResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(pastagemService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PastagemDTO.PastagemResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pastagemService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PastagemDTO.PastagemResponse> cadastrar(
            @Valid @RequestBody PastagemDTO.PastagemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pastagemService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<PastagemDTO.PastagemResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody PastagemDTO.PastagemRequest request) {
        return ResponseEntity.ok(pastagemService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pastagemService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
