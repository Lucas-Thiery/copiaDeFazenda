package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.AnimalDTO;
import com.ufs.gestaofazenda.Service.AnimalService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/animais", "/animais"})
@RequiredArgsConstructor
public class AnimalController {
    private final AnimalService animalService;

    @GetMapping
    public ResponseEntity<List<AnimalDTO.AnimalResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(animalService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalDTO.AnimalResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AnimalDTO.AnimalResponse> cadastrar(
            @Valid @RequestBody AnimalDTO.AnimalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.cadastrar(request));
    }

    @PostMapping("/{id}/transferencias")
    public ResponseEntity<AnimalDTO.AnimalResponse> transferir(
            @PathVariable Long id,
            @Valid @RequestBody
                    com.ufs.gestaofazenda.DTO.TransferenciaDTO.TransferenciaRequest request) {
        return ResponseEntity.ok(animalService.transferir(id, request));
    }
}
