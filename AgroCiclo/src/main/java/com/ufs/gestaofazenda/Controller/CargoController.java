package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.CargoDTO;
import com.ufs.gestaofazenda.Service.CargoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/cargos", "/cargos"})
@RequiredArgsConstructor
public class CargoController {
    private final CargoService cargoService;

    @GetMapping
    public ResponseEntity<List<CargoDTO.CargoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(cargoService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoDTO.CargoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cargoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CargoDTO.CargoResponse> cadastrar(
            @Valid @RequestBody CargoDTO.CargoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cargoService.cadastrar(request));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<CargoDTO.CargoResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody CargoDTO.CargoRequest request) {
        return ResponseEntity.ok(cargoService.atualizar(id, request));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cargoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
