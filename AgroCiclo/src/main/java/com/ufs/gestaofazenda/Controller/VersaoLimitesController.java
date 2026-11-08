package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.VersaoLimitesDTO;
import com.ufs.gestaofazenda.Service.VersaoLimitesService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/versoes-limites", "/versoes-limites"})
@RequiredArgsConstructor
public class VersaoLimitesController {
    private final VersaoLimitesService versaoLimitesService;

    @GetMapping
    public ResponseEntity<List<VersaoLimitesDTO.VersaoLimitesResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(versaoLimitesService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersaoLimitesDTO.VersaoLimitesResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(versaoLimitesService.buscarPorId(id));
    }
}
