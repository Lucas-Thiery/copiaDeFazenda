package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.ItemCompraDTO;
import com.ufs.gestaofazenda.Service.ItemCompraService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/itens-compra", "/itens-compra"})
@RequiredArgsConstructor
public class ItemCompraController {
    private final ItemCompraService itemCompraService;

    @GetMapping
    public ResponseEntity<List<ItemCompraDTO.ItemCompraResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(itemCompraService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemCompraDTO.ItemCompraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemCompraService.buscarPorId(id));
    }
}
