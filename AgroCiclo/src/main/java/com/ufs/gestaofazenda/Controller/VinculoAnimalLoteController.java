package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.VinculoAnimalLoteDTO;
import com.ufs.gestaofazenda.Service.VinculoAnimalLoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/vinculos-animal-lote", "/vinculos-animal-lote"})
@RequiredArgsConstructor
public class VinculoAnimalLoteController {
    private final VinculoAnimalLoteService vinculoAnimalLoteService;

    @GetMapping
    public ResponseEntity<List<VinculoAnimalLoteDTO.VinculoAnimalLoteResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(vinculoAnimalLoteService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VinculoAnimalLoteDTO.VinculoAnimalLoteResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(vinculoAnimalLoteService.buscarPorId(id));
    }
}
