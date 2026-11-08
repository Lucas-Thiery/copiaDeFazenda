package com.ufs.gestaofazenda.Service;

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

    @Transactional
    public void inicializar(SetupDTO.SetupRequest request) {
        entityManager
                .createNativeQuery("select id from trava_setup where id = 1 for update")
                .getSingleResult();
        if (usuarioRepository.count() > 0)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A configuração inicial já foi concluída.");
        Fazenda fazenda = new Fazenda();
        fazenda.setNome(request.getPropriedadeNome().trim());
        fazenda.setAreaHectares(request.getAreaHectares());
        fazenda.setLocalizacao(request.getLocalizacao().trim());
        fazenda.setResponsavel(request.getResponsavel().trim());
        fazendaRepository.save(fazenda);
        Cargo cargo = new Cargo();
        cargo.setFazenda(fazenda);
        cargo.setNome("Proprietário");
        cargo.setAtribuicoes("Administração da fazenda");
        cargo.setCustoHoraReferencia(BigDecimal.ZERO);
        cargoRepository.save(cargo);
        Funcionario funcionario = new Funcionario();
        funcionario.setFazenda(fazenda);
        funcionario.setCargo(cargo);
        funcionario.setNome(request.getNomeProprietario());
        funcionario.setHorarioTrabalho("Não informado");
        funcionario.setCustoHoraEfetivo(BigDecimal.ZERO);
        funcionarioRepository.save(funcionario);
        Usuario usuario = new Usuario();
        usuario.setFuncionario(funcionario);
        usuario.setLogin(request.getLogin().trim());
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        usuario.setPerfil(Perfil.PROPRIETARIO);
        usuario.getFazendas().add(fazenda);
        usuarioRepository.save(usuario);
        registrar(fazenda.getFazendaId(), "Fazenda", fazenda.getFazendaId());
        registrar(fazenda.getFazendaId(), "Cargo", cargo.getCargoId());
        registrar(fazenda.getFazendaId(), "Funcionario", funcionario.getFuncionarioId());
        registrar(fazenda.getFazendaId(), "Usuario", usuario.getUsuarioId());
    }

    private void registrar(Long codFazenda, String entidade, Long id) {
        Auditoria evento = new Auditoria();
        evento.setCodFazenda(codFazenda);
        evento.setAutor("configuracao-inicial");
        evento.setTipoEvento("INCLUSAO");
        evento.setEntidade(entidade);
        evento.setCodRegistro(id);
        auditoriaRepository.save(evento);
    }
}
