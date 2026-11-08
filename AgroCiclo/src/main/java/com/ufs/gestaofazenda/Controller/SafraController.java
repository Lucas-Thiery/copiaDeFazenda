package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.SafraDTO;
import com.ufs.gestaofazenda.Service.SafraService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/safras", "/safras"})
@RequiredArgsConstructor
public class SafraController {
    private final SafraService safraService;

    @GetMapping
    public ResponseEntity<List<SafraDTO.SafraResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(safraService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SafraDTO.SafraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(safraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SafraDTO.SafraResponse> cadastrar(
            @Valid @RequestBody SafraDTO.SafraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(safraService.cadastrar(request));
    }
}
