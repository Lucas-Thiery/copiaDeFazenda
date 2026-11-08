package com.ufs.gestaofazenda.Config;

import com.ufs.gestaofazenda.Repository.UsuarioRepository;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class ContaAtivaFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var usuario = usuarioRepository.findByLoginIgnoreCase(auth.getName());
            boolean ativo =
                    usuario.filter(
                                    u ->
                                            u.isAtivo()
                                                    && u.getFuncionario().isAtivo()
                                                    && auth.getAuthorities().stream()
                                                            .anyMatch(
                                                                    a ->
                                                                            a.getAuthority()
                                                                                    .equals(
                                                                                            "ROLE_"
                                                                                                    + u.getPerfil()
                                                                                                            .name())))
                            .isPresent();
            if (!ativo) {
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                SecurityContextHolder.clearContext();
                response.sendError(401, "Conta inativa ou perfil alterado. Faça login novamente.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
