package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.Entity.Fazenda;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SessaoController {
    private final ContextoUsuario contexto;

    @GetMapping("/api/sessao")
    public Map<String, Object> atual() {
        var usuario = contexto.usuario();
        var sessao = new LinkedHashMap<String, Object>();
        sessao.put("login", usuario.getLogin());
        sessao.put("perfil", usuario.getPerfil());
        sessao.put("usuarioId", usuario.getUsuarioId());
        sessao.put("primeiraFazendaPendente", usuario.primeiraFazendaPendente());
        sessao.put("codFazenda", usuario.primeiraFazendaPendente() ? null : contexto.codFazenda());
        sessao.put("codFazendas", usuario.getFazendas().stream().map(Fazenda::getFazendaId).toList());
        return sessao;
    }

    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
}
