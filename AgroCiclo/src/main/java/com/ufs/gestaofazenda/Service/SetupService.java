package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.CadastroProprietarioDTO;
import com.ufs.gestaofazenda.DTO.FazendaDTO;
import com.ufs.gestaofazenda.DTO.SetupDTO;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SetupService {
    private final FazendaRepository fazendaRepository;
    private final CargoRepository cargoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final ContextoUsuario contexto;

    @Transactional
    public void inicializar(SetupDTO.SetupRequest request) {
        bloquearCadastro();
        if (usuarioRepository.count() > 0)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A configuração inicial já foi concluída.");
        Fazenda fazenda = criarFazenda(request.getPropriedadeNome(), request.getAreaHectares(),
                request.getLocalizacao(), request.getResponsavel(), "configuracao-inicial");
        Funcionario funcionario = criarFuncionario(fazenda, request.getNomeProprietario(),
                "configuracao-inicial");
        Usuario usuario = new Usuario();
        usuario.setFuncionario(funcionario);
        usuario.setLogin(request.getLogin().trim());
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        usuario.setPerfil(Perfil.PROPRIETARIO);
        usuario.getFazendas().add(fazenda);
        usuarioRepository.save(usuario);
        registrar(fazenda.getFazendaId(), "Usuario", usuario.getUsuarioId(), "configuracao-inicial");
    }

    @Transactional
    public void cadastrarProprietario(CadastroProprietarioDTO request) {
        if (request.senha().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use uma senha de até 72 bytes.");
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String hash = passwordEncoder.encode(request.senha());
        bloquearCadastro();
        if (usuarioRepository.findByLoginIgnoreCase(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma conta para este e-mail.");
        Usuario usuario = new Usuario();
        usuario.setNomeProprietario(request.nomeCompleto().trim());
        usuario.setLogin(email);
        usuario.setEmail(email);
        usuario.setSenhaHash(hash);
        usuario.setPerfil(Perfil.PROPRIETARIO);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Long cadastrarPrimeiraFazenda(FazendaDTO.FazendaRequest request) {
        bloquearCadastro();
        Usuario usuario = contexto.usuario();
        if (!usuario.primeiraFazendaPendente())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A primeira fazenda desta conta já foi cadastrada.");
        String autor = usuario.getLogin();
        Fazenda fazenda = criarFazenda(request.getNome(), request.getAreaHectares(),
                request.getLocalizacao(), request.getResponsavel(), autor);
        usuario.setFuncionario(criarFuncionario(fazenda, usuario.getNomeProprietario(), autor));
        usuario.getFazendas().add(fazenda);
        registrar(fazenda.getFazendaId(), "Usuario", usuario.getUsuarioId(), autor);
        return fazenda.getFazendaId();
    }

    private void bloquearCadastro() {
        entityManager.createNativeQuery("select id from trava_setup where id = 1 for update")
                .getSingleResult();
    }

    private Fazenda criarFazenda(String nome, BigDecimal area, String localizacao,
            String responsavel, String autor) {
        Fazenda fazenda = new Fazenda();
        fazenda.setNome(nome.trim());
        fazenda.setAreaHectares(area);
        fazenda.setLocalizacao(localizacao.trim());
        fazenda.setResponsavel(responsavel.trim());
        fazendaRepository.save(fazenda);
        registrar(fazenda.getFazendaId(), "Fazenda", fazenda.getFazendaId(), autor);
        return fazenda;
    }

    private Funcionario criarFuncionario(Fazenda fazenda, String nome, String autor) {
        Cargo cargo = new Cargo();
        cargo.setFazenda(fazenda);
        cargo.setNome("Proprietário");
        cargo.setAtribuicoes("Administração da fazenda");
        cargo.setCustoHoraReferencia(BigDecimal.ZERO);
        cargoRepository.save(cargo);
        Funcionario funcionario = new Funcionario();
        funcionario.setFazenda(fazenda);
        funcionario.setCargo(cargo);
        funcionario.setNome(nome);
        funcionario.setHorarioTrabalho("Não informado");
        funcionario.setCustoHoraEfetivo(BigDecimal.ZERO);
        funcionarioRepository.save(funcionario);
        registrar(fazenda.getFazendaId(), "Cargo", cargo.getCargoId(), autor);
        registrar(fazenda.getFazendaId(), "Funcionario", funcionario.getFuncionarioId(), autor);
        return funcionario;
    }

    private void registrar(Long codFazenda, String entidade, Long id, String autor) {
        Auditoria evento = new Auditoria();
        evento.setCodFazenda(codFazenda);
        evento.setAutor(autor);
        evento.setTipoEvento("INCLUSAO");
        evento.setEntidade(entidade);
        evento.setCodRegistro(id);
        auditoriaRepository.save(evento);
    }
}
