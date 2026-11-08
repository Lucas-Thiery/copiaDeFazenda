package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.MaquinaDTO;
import com.ufs.gestaofazenda.Service.MaquinaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/maquinas", "/maquinas"})
@RequiredArgsConstructor
public class MaquinaController {
    private final MaquinaService maquinaService;

    @GetMapping
    public ResponseEntity<List<MaquinaDTO.MaquinaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(maquinaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaquinaDTO.MaquinaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(maquinaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MaquinaDTO.MaquinaResponse> cadastrar(
            @Valid @RequestBody MaquinaDTO.MaquinaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maquinaService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<MaquinaDTO.MaquinaResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody MaquinaDTO.MaquinaRequest request) {
        return ResponseEntity.ok(maquinaService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        maquinaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
