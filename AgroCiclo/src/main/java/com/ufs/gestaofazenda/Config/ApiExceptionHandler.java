package com.ufs.gestaofazenda.Config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validacao(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "erros",
                                e.getBindingResult().getFieldErrors().stream()
                                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                                        .toList()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integridade(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "mensagem",
                                "Operação bloqueada por duplicidade, histórico ou vínculo"
                                    + " dependente."));
    }
}
