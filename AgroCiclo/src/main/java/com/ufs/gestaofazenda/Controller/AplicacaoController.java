package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.AplicacaoDTO;
import com.ufs.gestaofazenda.Service.AplicacaoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/aplicacoes", "/aplicacoes"})
@RequiredArgsConstructor
public class AplicacaoController {
    private final AplicacaoService aplicacaoService;

    @GetMapping
    public ResponseEntity<List<AplicacaoDTO.AplicacaoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(aplicacaoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AplicacaoDTO.AplicacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(aplicacaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AplicacaoDTO.AplicacaoResponse> cadastrar(
            @Valid @RequestBody AplicacaoDTO.AplicacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aplicacaoService.cadastrar(request));
    }
}
