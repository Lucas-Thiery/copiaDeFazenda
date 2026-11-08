package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.Service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {
    private final RelatorioService relatorioService;
    private final ExportacaoService exportacaoService;

    @GetMapping("/{tipo}")
    public ResponseEntity<?> gerar(
            @PathVariable String tipo,
            @RequestParam LocalDate dtaInicio,
            @RequestParam LocalDate dtaFim,
            @RequestParam(required = false) Long codSafra,
            @RequestParam(required = false) Long codLote,
            @RequestParam(required = false) Long codAnimal,
            @RequestParam(defaultValue = "json") String formato) {
        var relatorio =
                relatorioService.gerar(tipo, dtaInicio, dtaFim, codSafra, codLote, codAnimal);
        return switch (formato) {
            case "json" -> ResponseEntity.ok(relatorio);
            case "csv" ->
                    ResponseEntity.ok()
                            .header(
                                    HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=agrociclo-" + tipo + ".csv")
                            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                            .body(exportacaoService.csv(relatorio));
            case "pdf" ->
                    ResponseEntity.ok()
                            .header(
                                    HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=agrociclo-" + tipo + ".pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(exportacaoService.pdf(relatorio));
            default ->
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Formato deve ser json, csv ou pdf.");
        };
    }
}
