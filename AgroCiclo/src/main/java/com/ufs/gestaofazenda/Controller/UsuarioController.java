package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.UsuarioDTO;
import com.ufs.gestaofazenda.Service.UsuarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/usuarios", "/usuarios"})
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO.UsuarioResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamanho) {
        return ResponseEntity.ok(usuarioService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO.UsuarioResponse> cadastrar(
            @Valid @RequestBody UsuarioDTO.UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(request));
    }

    @PatchMapping("/{id}/desativacao")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> atualizarEmail(
            @PathVariable Long id, @Valid @RequestBody UsuarioDTO.EmailRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarEmail(id, request));
    }
}
