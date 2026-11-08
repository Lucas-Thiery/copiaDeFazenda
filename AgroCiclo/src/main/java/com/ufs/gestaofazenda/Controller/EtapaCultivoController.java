package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.EtapaCultivoDTO;
import com.ufs.gestaofazenda.Service.EtapaCultivoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/etapas-cultivo", "/etapas-cultivo"})
@RequiredArgsConstructor
public class EtapaCultivoController {
    private final EtapaCultivoService etapaCultivoService;

    @GetMapping
    public ResponseEntity<List<EtapaCultivoDTO.EtapaCultivoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(etapaCultivoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtapaCultivoDTO.EtapaCultivoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(etapaCultivoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EtapaCultivoDTO.EtapaCultivoResponse> cadastrar(
            @Valid @RequestBody EtapaCultivoDTO.EtapaCultivoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(etapaCultivoService.cadastrar(request));
    }
}
