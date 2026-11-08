package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.SetupDTO;
import com.ufs.gestaofazenda.Service.SetupService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {
    private final SetupService setupService;

    @Value("${agrociclo.setup-token:}")
    private String setupToken;

    @PostMapping("/inicial")
    public ResponseEntity<?> inicial(
            @RequestHeader("X-Setup-Token") String token,
            @Valid @RequestBody SetupDTO.SetupRequest request) {
        if (setupToken.isBlank()
                || !MessageDigest.isEqual(
                        setupToken.getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Token de configuração inválido ou não configurado.");
        setupService.inicializar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Configuração inicial concluída. Faça login."));
    }
}
