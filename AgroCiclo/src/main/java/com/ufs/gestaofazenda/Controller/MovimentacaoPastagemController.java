package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.MovimentacaoPastagemDTO;
import com.ufs.gestaofazenda.Service.MovimentacaoPastagemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/movimentacoes-pastagem", "/movimentacoes-pastagem"})
@RequiredArgsConstructor
public class MovimentacaoPastagemController {
    private final MovimentacaoPastagemService movimentacaoPastagemService;

    @GetMapping
    public ResponseEntity<List<MovimentacaoPastagemDTO.MovimentacaoPastagemResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(movimentacaoPastagemService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoPastagemDTO.MovimentacaoPastagemResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(movimentacaoPastagemService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MovimentacaoPastagemDTO.MovimentacaoPastagemResponse> cadastrar(
            @Valid @RequestBody MovimentacaoPastagemDTO.MovimentacaoPastagemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimentacaoPastagemService.cadastrar(request));
    }
}
