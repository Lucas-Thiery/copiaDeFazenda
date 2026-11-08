package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.*;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final FazendaRepository fazendaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public List<UsuarioDTO.UsuarioResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return usuarioRepository.findDistinctByFazendasFazendaId(contexto.codFazenda()).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public UsuarioDTO.UsuarioResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public UsuarioDTO.UsuarioResponse cadastrar(UsuarioDTO.UsuarioRequest request) {
        contexto.bloquearFazenda();

        Usuario entidade = new Usuario();

        aplicarRequestNaEntidade(entidade, request);
        entidade = usuarioRepository.saveAndFlush(entidade);

        auditoriaService.registrar("INCLUSAO", "Usuario", entidade.getUsuarioId());
        return converterParaResponse(entidade);
    }

    private Usuario buscarEntidadePorId(Long id) {
        Usuario entidade =
                usuarioRepository
                        .findById(id)
                        .filter(
                                u ->
                                        u.getFazendas().stream()
                                                .anyMatch(
                                                        f ->
                                                                f.getFazendaId()
                                                                        .equals(
                                                                                contexto
                                                                                        .codFazenda())))
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Usuario não encontrado nesta fazenda."));

        return entidade;
    }

    private UsuarioDTO.UsuarioResponse converterParaResponse(Usuario entidade) {
        UsuarioDTO.UsuarioResponse response = new UsuarioDTO.UsuarioResponse();
        response.setEmail(entidade.getEmail());
        response.setUsuarioId(entidade.getUsuarioId());
        response.setCodFuncionario(
                entidade.getFuncionario() == null
                        ? null
                        : entidade.getFuncionario().getFuncionarioId());
        response.setLogin(entidade.getLogin());
        response.setPerfil(entidade.getPerfil());
        response.setAtivo(entidade.isAtivo());
        response.setDtaCadastro(entidade.getDtaCadastro());
        response.setCodFazendas(
                entidade.getFazendas().stream()
                        .map(Fazenda::getFazendaId)
                        .collect(java.util.stream.Collectors.toSet()));
        return response;
    }

    private void aplicarRequestNaEntidade(Usuario entidade, UsuarioDTO.UsuarioRequest request) {
        entidade.setFuncionario(
                request.getCodFuncionario() == null
                        ? null
                        : funcionarioRepository
                                .findById(request.getCodFuncionario())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(
                                        () ->
                                                erro(
                                                        "Funcionario inexistente ou de outra"
                                                            + " fazenda.")));
        entidade.setLogin(request.getLogin().trim());
        entidade.setEmail(request.getEmail());
        entidade.setPerfil(request.getPerfil());
        if (usuarioRepository.existsByFuncionarioFuncionarioId(
                entidade.getFuncionario().getFuncionarioId()))
            throw erro("Funcionário já possui conta.");
        if (usuarioRepository.findByLoginIgnoreCase(entidade.getLogin()).isPresent())
            throw erro("Login já utilizado.");
        entidade.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        if (request.getCodFazendas() == null || request.getCodFazendas().isEmpty())
            throw erro("Informe as fazendas da conta.");
        for (Long codFazenda : request.getCodFazendas()) {
            Fazenda fazenda =
                    contexto.usuario().getFazendas().stream()
                            .filter(f -> f.getFazendaId().equals(codFazenda))
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            erro(
                                                    "Não é permitido conceder acesso a fazenda não"
                                                        + " administrada."));
            entidade.getFazendas().add(fazenda);
        }
        if (entidade.getFazendas().stream()
                .noneMatch(
                        f ->
                                f.getFazendaId()
                                        .equals(
                                                entidade.getFuncionario()
                                                        .getFazenda()
                                                        .getFazendaId())))
            throw erro("A fazenda do funcionário deve integrar seus vínculos.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void desativar(Long id) {
        // Serialize account changes globally as a user may belong to multiple farms.
        contexto.bloquearFazenda();
        Usuario usuario = buscarEntidadePorId(id);
        if (!usuario.isAtivo()) return;
        if (usuario.getPerfil() == Perfil.PROPRIETARIO) {
            for (Fazenda fazenda : usuario.getFazendas()) {
                long ativos =
                        usuarioRepository
                                .findDistinctByFazendasFazendaId(fazenda.getFazendaId())
                                .stream()
                                .filter(u -> u.isAtivo() && u.getPerfil() == Perfil.PROPRIETARIO)
                                .count();
                if (ativos <= 1)
                    throw erro(
                            "Não é permitido desativar o último Proprietário ativo de uma"
                                + " fazenda.");
            }
        }
        usuario.setAtivo(false);
        auditoriaService.registrar("DESATIVACAO", "Usuario", id);
    }

    @Transactional
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public UsuarioDTO.UsuarioResponse atualizarEmail(Long id, UsuarioDTO.EmailRequest request) {
        contexto.bloquearFazenda();
        Usuario usuario = buscarEntidadePorId(id);
        usuario.setEmail(request.getEmail());
        auditoriaService.registrar("ALTERACAO", "Usuario", id);
        return converterParaResponse(usuario);
    }
}
