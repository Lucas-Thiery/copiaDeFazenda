package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.CalendarioSanitarioDTO;
import com.ufs.gestaofazenda.Service.CalendarioSanitarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/calendarios-sanitarios", "/calendarios-sanitarios"})
@RequiredArgsConstructor
public class CalendarioSanitarioController {
    private final CalendarioSanitarioService calendarioSanitarioService;

    @GetMapping
    public ResponseEntity<List<CalendarioSanitarioDTO.CalendarioSanitarioResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(calendarioSanitarioService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarioSanitarioDTO.CalendarioSanitarioResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(calendarioSanitarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CalendarioSanitarioDTO.CalendarioSanitarioResponse> cadastrar(
            @Valid @RequestBody CalendarioSanitarioDTO.CalendarioSanitarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calendarioSanitarioService.cadastrar(request));
    }
}
