package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.TarefaDTO;
import com.ufs.gestaofazenda.Service.TarefaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/tarefas", "/tarefas"})
@RequiredArgsConstructor
public class TarefaController {
    private final TarefaService tarefaService;

    @GetMapping
    public ResponseEntity<List<TarefaDTO.TarefaResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(tarefaService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaDTO.TarefaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TarefaDTO.TarefaResponse> cadastrar(
            @Valid @RequestBody TarefaDTO.TarefaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaService.cadastrar(request));
    }

    @PostMapping("/{id}/concluir")
    public ResponseEntity<TarefaDTO.TarefaResponse> concluir(
            @PathVariable Long id,
            @Valid @RequestBody
                    com.ufs.gestaofazenda.DTO.ExecucaoTarefaDTO.ExecucaoTarefaRequest request) {
        return ResponseEntity.ok(tarefaService.concluir(id, request));
    }
}
