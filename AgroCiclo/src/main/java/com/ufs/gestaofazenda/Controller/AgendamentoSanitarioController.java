package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.AgendamentoSanitarioDTO;
import com.ufs.gestaofazenda.Service.AgendamentoSanitarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/agendamentos-sanitarios", "/agendamentos-sanitarios"})
@RequiredArgsConstructor
public class AgendamentoSanitarioController {
    private final AgendamentoSanitarioService agendamentoSanitarioService;

    @GetMapping
    public ResponseEntity<List<AgendamentoSanitarioDTO.AgendamentoSanitarioResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(agendamentoSanitarioService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoSanitarioDTO.AgendamentoSanitarioResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(agendamentoSanitarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AgendamentoSanitarioDTO.AgendamentoSanitarioResponse> cadastrar(
            @Valid @RequestBody AgendamentoSanitarioDTO.AgendamentoSanitarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendamentoSanitarioService.cadastrar(request));
    }
}
