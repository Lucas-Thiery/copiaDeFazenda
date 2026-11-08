package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.LancamentoDTO;
import com.ufs.gestaofazenda.Service.LancamentoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/lancamentos", "/lancamentos"})
@RequiredArgsConstructor
public class LancamentoController {
    private final LancamentoService lancamentoService;

    @GetMapping
    public ResponseEntity<List<LancamentoDTO.LancamentoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(lancamentoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LancamentoDTO.LancamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(lancamentoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<LancamentoDTO.LancamentoResponse> cadastrar(
            @Valid @RequestBody LancamentoDTO.LancamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoService.cadastrar(request));
    }
}
