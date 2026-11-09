package com.ufs.gestaofazenda.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufs.gestaofazenda.Entity.Solicitacao;
import com.ufs.gestaofazenda.Repository.SolicitacaoRepository;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class ReenvioAspect {
    private final ContextoUsuario contexto;
    private final jakarta.persistence.EntityManager entityManager;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private final PlatformTransactionManager transactionManager;

    @Around(
            "execution(public * com.ufs.gestaofazenda.Controller.*.*(..)) &&"
                + " !within(com.ufs.gestaofazenda.Controller.SetupController)"
                + " && !within(com.ufs.gestaofazenda.Controller.CadastroProprietarioController)")
    public Object gravacao(ProceedingJoinPoint chamada) throws Throwable {
        if (!Set.of("POST", "PUT", "PATCH", "DELETE").contains(request.getMethod()))
            return chamada.proceed();
        String chave = request.getHeader("Idempotency-Key");
        if (chave == null || chave.isBlank() || chave.length() > 200)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Envie Idempotency-Key (até 200 caracteres), preservando-a em reenvios.");
        return new TransactionTemplate(transactionManager)
                .execute(
                        status -> {
                            try {
                                if (chamada.getTarget()
                                                instanceof
                                                com.ufs.gestaofazenda.Controller.UsuarioController
                                        || chamada.getTarget()
                                                instanceof
                                                com.ufs.gestaofazenda.Controller.FazendaController)
                                    entityManager
                                            .createNativeQuery(
                                                    "select id from trava_setup where id = 1 for"
                                                        + " update")
                                            .getSingleResult();
                                contexto.bloquearFazenda();
                                Long fazenda = contexto.codFazenda();
                                Long usuario = contexto.usuario().getUsuarioId();
                                String perfil = contexto.usuario().getPerfil().name();
                                String dados =
                                        request.getMethod()
                                                + request.getRequestURI()
                                                + objectMapper.writeValueAsString(
                                                        chamada.getArgs());
                                String impressao =
                                        HexFormat.of()
                                                .formatHex(
                                                        MessageDigest.getInstance("SHA-256")
                                                                .digest(
                                                                        dados.getBytes(
                                                                                StandardCharsets
                                                                                        .UTF_8)));
                                var anterior =
                                        solicitacaoRepository.findByCodFazendaAndCodUsuarioAndChave(
                                                fazenda, usuario, chave);
                                if (anterior.isPresent()) {
                                    Solicitacao registro = anterior.get();
                                    if (!registro.getImpressao().equals(impressao)
                                            || !registro.getPerfil().equals(perfil))
                                        throw new ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Chave já utilizada com outros dados ou perfil.");
                                    var retorno =
                                            ((MethodSignature) chamada.getSignature())
                                                    .getMethod()
                                                    .getGenericReturnType();
                                    var tipo =
                                            retorno instanceof ParameterizedType p
                                                    ? p.getActualTypeArguments()[0]
                                                    : Object.class;
                                    Object corpo =
                                            registro.getResposta() == null
                                                    ? null
                                                    : objectMapper.readValue(
                                                            registro.getResposta(),
                                                            objectMapper.constructType(tipo));
                                    return ResponseEntity.status(registro.getStatus()).body(corpo);
                                }
                                ResponseEntity<?> resposta = (ResponseEntity<?>) chamada.proceed();
                                Solicitacao registro = new Solicitacao();
                                registro.setCodFazenda(fazenda);
                                registro.setCodUsuario(usuario);
                                registro.setChave(chave);
                                registro.setImpressao(impressao);
                                registro.setPerfil(perfil);
                                registro.setStatus(resposta.getStatusCode().value());
                                registro.setResposta(
                                        resposta.getBody() == null
                                                ? null
                                                : objectMapper.writeValueAsString(
                                                        resposta.getBody()));
                                solicitacaoRepository.saveAndFlush(registro);
                                return resposta;
                            } catch (RuntimeException e) {
                                throw e;
                            } catch (Throwable e) {
                                throw new IllegalStateException(
                                        "Falha ao concluir a solicitação.", e);
                            }
                        });
    }
}
