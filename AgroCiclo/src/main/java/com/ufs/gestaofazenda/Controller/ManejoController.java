package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ManejoDTO;
import com.ufs.gestaofazenda.Service.ManejoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/manejos", "/manejos"})
@RequiredArgsConstructor
public class ManejoController {
    private final ManejoService manejoService;

    @GetMapping
    public ResponseEntity<List<ManejoDTO.ManejoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(manejoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManejoDTO.ManejoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(manejoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ManejoDTO.ManejoResponse> cadastrar(
            @Valid @RequestBody ManejoDTO.ManejoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manejoService.cadastrar(request));
    }
}
