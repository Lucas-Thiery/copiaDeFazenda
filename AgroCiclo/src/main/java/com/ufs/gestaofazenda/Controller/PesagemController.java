package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.PesagemDTO;
import com.ufs.gestaofazenda.Service.PesagemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/pesagens", "/pesagens"})
@RequiredArgsConstructor
public class PesagemController {
    private final PesagemService pesagemService;

    @GetMapping
    public ResponseEntity<List<PesagemDTO.PesagemResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(pesagemService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesagemDTO.PesagemResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pesagemService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PesagemDTO.PesagemResponse> cadastrar(
            @Valid @RequestBody PesagemDTO.PesagemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pesagemService.cadastrar(request));
    }

    @GetMapping("/evolucao")
    public ResponseEntity<List<PesagemDTO.PesagemResponse>> evolucao(
            @RequestParam(required = false) Long codAnimal,
            @RequestParam(required = false) Long codLote,
            @RequestParam java.time.LocalDate dtaInicio,
            @RequestParam java.time.LocalDate dtaFim) {
        return ResponseEntity.ok(pesagemService.evolucao(codAnimal, codLote, dtaInicio, dtaFim));
    }
}
