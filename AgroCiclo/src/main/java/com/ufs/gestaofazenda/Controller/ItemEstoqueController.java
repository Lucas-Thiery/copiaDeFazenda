package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ItemEstoqueDTO;
import com.ufs.gestaofazenda.Service.ItemEstoqueService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/estoque/itens", "/estoque/itens"})
@RequiredArgsConstructor
public class ItemEstoqueController {
    private final ItemEstoqueService itemEstoqueService;

    @GetMapping
    public ResponseEntity<List<ItemEstoqueDTO.ItemEstoqueResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(itemEstoqueService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemEstoqueDTO.ItemEstoqueResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemEstoqueService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ItemEstoqueDTO.ItemEstoqueResponse> cadastrar(
            @Valid @RequestBody ItemEstoqueDTO.ItemEstoqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemEstoqueService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<ItemEstoqueDTO.ItemEstoqueResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody ItemEstoqueDTO.ItemEstoqueRequest request) {
        return ResponseEntity.ok(itemEstoqueService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        itemEstoqueService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
