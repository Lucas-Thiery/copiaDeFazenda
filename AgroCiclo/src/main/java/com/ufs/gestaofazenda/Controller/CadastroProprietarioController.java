package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.CadastroProprietarioDTO;
import com.ufs.gestaofazenda.DTO.FazendaDTO;
import com.ufs.gestaofazenda.Service.SetupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cadastro")
@RequiredArgsConstructor
public class CadastroProprietarioController {
    private final SetupService setupService;

    @PostMapping("/proprietario")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> cadastrar(@Valid @RequestBody CadastroProprietarioDTO request) {
        setupService.cadastrarProprietario(request);
        return Map.of("mensagem", "Conta criada. Entre para cadastrar sua fazenda.");
    }

    @PostMapping("/primeira-fazenda")
    @PreAuthorize("hasRole('PROPRIETARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> cadastrarPrimeiraFazenda(
            @Valid @RequestBody FazendaDTO.FazendaRequest request) {
        return Map.of("fazendaId", setupService.cadastrarPrimeiraFazenda(request));
    }
}
