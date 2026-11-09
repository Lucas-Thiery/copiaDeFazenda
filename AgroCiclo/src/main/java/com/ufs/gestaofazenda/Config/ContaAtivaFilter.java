package com.ufs.gestaofazenda.Config;

import com.ufs.gestaofazenda.Repository.UsuarioRepository;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class ContaAtivaFilter extends OncePerRequestFilter {
    private static final String CADASTRO_FAZENDA =
            "/fazendaweb/cadastroGerais/cadastrarPropriedade.html";
    private static final Set<String> ROTAS_PRIMEIRA_FAZENDA = Set.of(
            "/", "/index.html", CADASTRO_FAZENDA, "/api/sessao", "/api/csrf",
            "/api/cadastro/primeira-fazenda", "/api/auth/logout",
            "/fazendaweb/login.html", "/fazendaweb/style.css");

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
                                            u.podeAutenticar()
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
            if (usuario.orElseThrow().primeiraFazendaPendente()) {
                String path = request.getServletPath();
                if (path.isEmpty()) path = request.getRequestURI();
                if (!ROTAS_PRIMEIRA_FAZENDA.contains(path)
                        && !path.startsWith("/fazendaweb/js/")
                        && !path.startsWith("/fazendaweb/imagens/")) {
                    if ("GET".equals(request.getMethod()) && path.endsWith(".html")) {
                        response.sendRedirect(request.getContextPath() + CADASTRO_FAZENDA);
                    } else {
                        response.sendError(403, "Cadastre sua primeira fazenda.");
                    }
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
