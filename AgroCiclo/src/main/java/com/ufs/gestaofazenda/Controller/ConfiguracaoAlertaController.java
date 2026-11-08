package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ConfiguracaoAlertaDTO;
import com.ufs.gestaofazenda.Service.ConfiguracaoAlertaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/configuracoes-alerta", "/configuracoes-alerta"})
@RequiredArgsConstructor
public class ConfiguracaoAlertaController {
    private final ConfiguracaoAlertaService configuracaoAlertaService;

    @GetMapping
    public ResponseEntity<List<ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(configuracaoAlertaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(configuracaoAlertaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse> cadastrar(
            @Valid @RequestBody ConfiguracaoAlertaDTO.ConfiguracaoAlertaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configuracaoAlertaService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<ConfiguracaoAlertaDTO.ConfiguracaoAlertaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ConfiguracaoAlertaDTO.ConfiguracaoAlertaRequest request) {
        return ResponseEntity.ok(configuracaoAlertaService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoAlertaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
