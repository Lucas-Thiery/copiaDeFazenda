package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.PartidaEstoqueDTO;
import com.ufs.gestaofazenda.Service.PartidaEstoqueService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/estoque/partidas", "/estoque/partidas"})
@RequiredArgsConstructor
public class PartidaEstoqueController {
    private final PartidaEstoqueService partidaEstoqueService;

    @GetMapping
    public ResponseEntity<List<PartidaEstoqueDTO.PartidaEstoqueResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(partidaEstoqueService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaEstoqueDTO.PartidaEstoqueResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(partidaEstoqueService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PartidaEstoqueDTO.PartidaEstoqueResponse> cadastrar(
            @Valid @RequestBody PartidaEstoqueDTO.PartidaEstoqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partidaEstoqueService.cadastrar(request));
    }
}
