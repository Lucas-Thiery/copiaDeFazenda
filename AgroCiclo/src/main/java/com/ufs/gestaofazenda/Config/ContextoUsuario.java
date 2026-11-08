package com.ufs.gestaofazenda.Config;

import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ContextoUsuario {
    private final UsuarioRepository usuarioRepository;
    private final FazendaRepository fazendaRepository;
    private final HttpServletRequest request;

    public Usuario usuario() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida.");
        return usuarioRepository
                .findByLoginIgnoreCase(auth.getName())
                .filter(u -> u.isAtivo() && u.getFuncionario().isAtivo())
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Conta inativa ou sessão inválida."));
    }

    public Fazenda fazenda() {
        Usuario usuario = usuario();
        String valor = request.getHeader("X-Fazenda-Id");
        Long codFazenda = usuario.getFuncionario().getFazenda().getFazendaId();
        if (valor != null) {
            try {
                codFazenda = Long.valueOf(valor);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Fazenda-Id inválido.");
            }
        }
        final Long escolhido = codFazenda;
        return usuario.getFazendas().stream()
                .filter(f -> f.getFazendaId().equals(escolhido) && f.isAtiva())
                .findFirst()
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN, "Fazenda não autorizada."));
    }

    public Long codFazenda() {
        return fazenda().getFazendaId();
    }

    public boolean proprietario() {
        return usuario().getPerfil() == Perfil.PROPRIETARIO;
    }

    public boolean operador() {
        return usuario().getPerfil() == Perfil.OPERADOR;
    }

    public void bloquearFazenda() {
        fazendaRepository.bloquear(codFazenda()).orElseThrow();
    }
}
