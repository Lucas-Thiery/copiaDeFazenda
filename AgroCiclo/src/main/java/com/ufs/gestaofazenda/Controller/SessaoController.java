package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.Config.ContextoUsuario;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SessaoController {
    private final ContextoUsuario contexto;

    @GetMapping("/api/sessao")
    public Map<String, Object> atual() {
        return Map.of(
                "login",
                contexto.usuario().getLogin(),
                "perfil",
                contexto.usuario().getPerfil(),
                "usuarioId",
                contexto.usuario().getUsuarioId(),
                "codFazenda",
                contexto.codFazenda(),
                "codFazendas",
                contexto.usuario().getFazendas().stream().map(f -> f.getFazendaId()).toList());
    }

    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
}
