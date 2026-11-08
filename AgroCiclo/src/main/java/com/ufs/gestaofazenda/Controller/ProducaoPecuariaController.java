package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ProducaoPecuariaDTO;
import com.ufs.gestaofazenda.Service.ProducaoPecuariaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/producoes-pecuarias", "/producoes-pecuarias"})
@RequiredArgsConstructor
public class ProducaoPecuariaController {
    private final ProducaoPecuariaService producaoPecuariaService;

    @GetMapping
    public ResponseEntity<List<ProducaoPecuariaDTO.ProducaoPecuariaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(producaoPecuariaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProducaoPecuariaDTO.ProducaoPecuariaResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(producaoPecuariaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProducaoPecuariaDTO.ProducaoPecuariaResponse> cadastrar(
            @Valid @RequestBody ProducaoPecuariaDTO.ProducaoPecuariaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(producaoPecuariaService.cadastrar(request));
    }
}
