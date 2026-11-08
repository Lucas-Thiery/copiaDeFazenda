package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.FornecedorDTO;
import com.ufs.gestaofazenda.Service.FornecedorService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/fornecedores", "/fornecedores"})
@RequiredArgsConstructor
public class FornecedorController {
    private final FornecedorService fornecedorService;

    @GetMapping
    public ResponseEntity<List<FornecedorDTO.FornecedorResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(fornecedorService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorDTO.FornecedorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<FornecedorDTO.FornecedorResponse> cadastrar(
            @Valid @RequestBody FornecedorDTO.FornecedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<FornecedorDTO.FornecedorResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody FornecedorDTO.FornecedorRequest request) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fornecedorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
