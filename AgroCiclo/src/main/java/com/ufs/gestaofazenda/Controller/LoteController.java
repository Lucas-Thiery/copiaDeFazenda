package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.LoteDTO;
import com.ufs.gestaofazenda.Service.LoteService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/lotes", "/lotes"})
@RequiredArgsConstructor
public class LoteController {
    private final LoteService loteService;

    @GetMapping
    public ResponseEntity<List<LoteDTO.LoteResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(loteService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteDTO.LoteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<LoteDTO.LoteResponse> cadastrar(
            @Valid @RequestBody LoteDTO.LoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loteService.cadastrar(request));
    }
}
