package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ManutencaoDTO;
import com.ufs.gestaofazenda.Service.ManutencaoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/manutencoes", "/manutencoes"})
@RequiredArgsConstructor
public class ManutencaoController {
    private final ManutencaoService manutencaoService;

    @GetMapping
    public ResponseEntity<List<ManutencaoDTO.ManutencaoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(manutencaoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManutencaoDTO.ManutencaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(manutencaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ManutencaoDTO.ManutencaoResponse> cadastrar(
            @Valid @RequestBody ManutencaoDTO.ManutencaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manutencaoService.cadastrar(request));
    }
}
