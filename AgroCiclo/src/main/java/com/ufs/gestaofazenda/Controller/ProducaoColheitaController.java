package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ProducaoColheitaDTO;
import com.ufs.gestaofazenda.Service.ProducaoColheitaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/producoes-colheita", "/producoes-colheita"})
@RequiredArgsConstructor
public class ProducaoColheitaController {
    private final ProducaoColheitaService producaoColheitaService;

    @GetMapping
    public ResponseEntity<List<ProducaoColheitaDTO.ProducaoColheitaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(producaoColheitaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProducaoColheitaDTO.ProducaoColheitaResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(producaoColheitaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProducaoColheitaDTO.ProducaoColheitaResponse> cadastrar(
            @Valid @RequestBody ProducaoColheitaDTO.ProducaoColheitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(producaoColheitaService.cadastrar(request));
    }
}
