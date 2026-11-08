package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.LiquidacaoDTO;
import com.ufs.gestaofazenda.Service.LiquidacaoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/liquidacoes", "/liquidacoes"})
@RequiredArgsConstructor
public class LiquidacaoController {
    private final LiquidacaoService liquidacaoService;

    @GetMapping
    public ResponseEntity<List<LiquidacaoDTO.LiquidacaoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(liquidacaoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiquidacaoDTO.LiquidacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(liquidacaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<LiquidacaoDTO.LiquidacaoResponse> cadastrar(
            @Valid @RequestBody LiquidacaoDTO.LiquidacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(liquidacaoService.cadastrar(request));
    }
}
