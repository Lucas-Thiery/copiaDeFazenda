package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.CompraDTO;
import com.ufs.gestaofazenda.Service.CompraService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/compras", "/compras"})
@RequiredArgsConstructor
public class CompraController {
    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<List<CompraDTO.CompraResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(compraService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO.CompraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompraDTO.CompraResponse> cadastrar(
            @Valid @RequestBody CompraDTO.CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.cadastrar(request));
    }
}
