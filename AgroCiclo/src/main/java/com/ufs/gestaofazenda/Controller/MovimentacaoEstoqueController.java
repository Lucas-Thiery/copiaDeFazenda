package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.MovimentacaoEstoqueDTO;
import com.ufs.gestaofazenda.Service.MovimentacaoEstoqueService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/estoque/movimentacoes", "/estoque/movimentacoes"})
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    @GetMapping
    public ResponseEntity<List<MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(movimentacaoEstoqueService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(movimentacaoEstoqueService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MovimentacaoEstoqueDTO.MovimentacaoEstoqueResponse> cadastrar(
            @Valid @RequestBody MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimentacaoEstoqueService.cadastrar(request));
    }
}
