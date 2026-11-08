package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.FuncionarioDTO;
import com.ufs.gestaofazenda.Service.FuncionarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/funcionarios", "/funcionarios"})
@RequiredArgsConstructor
public class FuncionarioController {
    private final FuncionarioService funcionarioService;

    @GetMapping
    public ResponseEntity<List<FuncionarioDTO.FuncionarioResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(funcionarioService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO.FuncionarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(funcionarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<FuncionarioDTO.FuncionarioResponse> cadastrar(
            @Valid @RequestBody FuncionarioDTO.FuncionarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(funcionarioService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<FuncionarioDTO.FuncionarioResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody FuncionarioDTO.FuncionarioRequest request) {
        return ResponseEntity.ok(funcionarioService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        funcionarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
