package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.AlertaDTO;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
public class AlertaService {
    private final ContextoUsuario contexto;
    private final ConfiguracaoAlertaRepository configuracaoAlertaRepository;
    private final AgendamentoSanitarioRepository agendamentoSanitarioRepository;
    private final PartidaEstoqueRepository partidaEstoqueRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final PlanoManutencaoRepository planoManutencaoRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<AlertaDTO.AlertaResponse> consultar(LocalDate referencia, String categoria) {
        List<AlertaDTO.AlertaResponse> alertas = new ArrayList<>();
        Long fazenda = contexto.codFazenda();
        var config =
                configuracaoAlertaRepository.findAllByFazendaFazendaId(fazenda).stream()
                        .findFirst()
                        .orElse(null);
        if (config == null)
            adicionar(
                    alertas,
                    null,
                    "CONFIGURACAO",
                    "Antecedências não configuradas; não há presunção de regularidade.",
                    referencia);
        var partidas = partidaEstoqueRepository.findAllByFazendaFazendaId(fazenda);
        for (var item : itemEstoqueRepository.findAllByFazendaFazendaId(fazenda)) {
            BigDecimal saldo =
                    partidas.stream()
                            .filter(
                                    p ->
                                            p.getItem()
                                                    .getItemEstoqueId()
                                                    .equals(item.getItemEstoqueId()))
                            .map(PartidaEstoque::getQuantidadeAtual)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (saldo.compareTo(item.getEstoqueMinimo()) < 0)
                adicionar(
                        alertas,
                        item.getItemEstoqueId(),
                        "ESTOQUE_MINIMO",
                        "Saldo abaixo do mínimo: " + item.getNome(),
                        referencia);
        }
        for (var p : partidas) {
            if (p.getQuantidadeAtual().signum() <= 0) continue;
            if (p.getDtaValidade() == null)
                adicionar(
                        alertas,
                        p.getPartidaEstoqueId(),
                        "VALIDADE_PENDENTE",
                        "Validade não informada.",
                        referencia);
            else if (config != null
                    && !p.getDtaValidade().isAfter(referencia.plusDays(config.getDiasValidade())))
                adicionar(
                        alertas,
                        p.getPartidaEstoqueId(),
                        "VALIDADE",
                        "Partida próxima do vencimento ou vencida.",
                        p.getDtaValidade());
        }
        if (config != null) {
            for (var a : agendamentoSanitarioRepository.findAllByFazendaFazendaId(fazenda)) {
                if (!a.isConcluido()
                        && !a.getDtaPrevista().isAfter(referencia.plusDays(config.getDiasVacina())))
                    adicionar(
                            alertas,
                            a.getAgendamentoSanitarioId(),
                            "SANITARIO",
                            "Ação sanitária pendente: " + a.getCalendario().getAcao(),
                            a.getDtaPrevista());
            }
            for (var p : planoManutencaoRepository.findAllByFazendaFazendaId(fazenda)) {
                if (p.isConcluido()) continue;
                boolean data =
                        p.getDtaPrevista() != null
                                && !p.getDtaPrevista()
                                        .isAfter(referencia.plusDays(config.getDiasManutencao()));
                BigDecimal margem =
                        "HORAS".equals(p.getMaquina().getUnidadeMedidor())
                                ? config.getMargemHoras()
                                : config.getMargemKm();
                boolean uso =
                        p.getLimiteMedidor() != null
                                && p.getMaquina().getLeitura() != null
                                && margem != null
                                && p.getMaquina()
                                                .getLeitura()
                                                .add(margem)
                                                .compareTo(p.getLimiteMedidor())
                                        >= 0;
                if (data || uso)
                    adicionar(
                            alertas,
                            p.getPlanoManutencaoId(),
                            "MANUTENCAO",
                            "Manutenção prevista por data ou uso: " + p.getServico(),
                            p.getDtaPrevista());
            }
        }
        if (categoria == null) return alertas;
        if (!Set.of(
                        "CONFIGURACAO",
                        "ESTOQUE_MINIMO",
                        "VALIDADE_PENDENTE",
                        "VALIDADE",
                        "SANITARIO",
                        "MANUTENCAO")
                .contains(categoria))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Categoria de alerta inválida.");
        return alertas.stream()
                .filter(
                        a ->
                                a.getCategoria().equals(categoria)
                                        || a.getCategoria().equals("CONFIGURACAO"))
                .toList();
    }

    public AlertaDTO.EnvioResponse enviar() {
        String email = contexto.usuario().getEmail();
        if (email == null || !email.matches("^[^@ ]+@[^@ ]+[.][^@ ]+$"))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cadastre um e-mail válido para o usuário antes do envio.");
        var response = new AlertaDTO.EnvioResponse();
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            response.setResultado("FALHA");
            response.setMensagem("Serviço de e-mail não configurado; alertas preservados.");
            return response;
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(email);
        mensagem.setSubject("AgroCiclo - alertas preventivos");
        mensagem.setText(
                consultar(LocalDate.now(), null).stream()
                        .map(a -> a.getCategoria() + ": " + a.getMotivo())
                        .collect(java.util.stream.Collectors.joining("\n")));
        try {
            sender.send(mensagem);
            response.setResultado("ACEITO");
            response.setMensagem(
                    "Solicitação aceita pelo serviço; entrega e leitura não comprovadas.");
        } catch (MailException e) {
            response.setResultado("NAO_CONFIRMADO");
            response.setMensagem(
                    "Envio não confirmado; alertas preservados. Não há reenvio automático. Consulte"
                        + " o provedor antes de uma nova tentativa.");
        }
        auditoriaService.registrar("ENVIO_ALERTAS", "Usuario", contexto.usuario().getUsuarioId());
        return response;
    }

    private void adicionar(
            List<AlertaDTO.AlertaResponse> lista,
            Long id,
            String categoria,
            String motivo,
            LocalDate data) {
        var alerta = new AlertaDTO.AlertaResponse();
        alerta.setCodRegistro(id);
        alerta.setCategoria(categoria);
        alerta.setMotivo(motivo);
        alerta.setDtaReferencia(data);
        lista.add(alerta);
    }
}
