package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.DTO.*;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final AplicacaoRepository aplicacaoRepository;
    private final AuditoriaService auditoriaService;
    private final ContextoUsuario contexto;
    private final LoteRepository loteRepository;
    private final PesagemRepository pesagemRepository;
    private final ProducaoPecuariaRepository producaoPecuariaRepository;
    private final VinculoAnimalLoteRepository vinculoAnimalLoteRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public List<AnimalDTO.AnimalResponse> listar(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 500)
            throw erro("Paginação inválida; use tamanho entre 1 e 500.");
        return animalRepository
                .findAllByFazendaFazendaId(contexto.codFazenda(), PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO','OPERADOR')")
    public AnimalDTO.AnimalResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public AnimalDTO.AnimalResponse cadastrar(AnimalDTO.AnimalRequest request) {
        contexto.bloquearFazenda();
        if (animalRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .anyMatch(
                        e ->
                                e.getIdentificacao()
                                        .equalsIgnoreCase(request.getIdentificacao().trim())))
            throw erro("Animal já cadastrado.");
        Animal entidade = new Animal();
        entidade.setFazenda(contexto.fazenda());
        aplicarRequestNaEntidade(entidade, request);
        entidade = animalRepository.saveAndFlush(entidade);
        VinculoAnimalLote vinculo = new VinculoAnimalLote();
        vinculo.setFazenda(entidade.getFazenda());
        vinculo.setAnimal(entidade);
        vinculo.setLote(entidade.getLoteAtual());
        vinculo.setDtaInicio(entidade.getDtaEntrada());
        vinculoAnimalLoteRepository.saveAndFlush(vinculo);
        Pesagem inicial = new Pesagem();
        inicial.setFazenda(entidade.getFazenda());
        inicial.setAnimal(entidade);
        inicial.setLote(entidade.getLoteAtual());
        inicial.setDtaPesagem(entidade.getDtaEntrada());
        inicial.setPeso(entidade.getPeso());
        inicial.setComposicao(entidade.getAnimalId().toString());
        inicial.setObservacao("Pesagem inicial do cadastro.");
        pesagemRepository.save(inicial);
        auditoriaService.registrar("INCLUSAO", "Animal", entidade.getAnimalId());
        return converterParaResponse(entidade);
    }

    private Animal buscarEntidadePorId(Long id) {
        Animal entidade =
                animalRepository
                        .findByAnimalIdAndFazendaFazendaId(id, contexto.codFazenda())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Animal não encontrado nesta fazenda."));

        return entidade;
    }

    private AnimalDTO.AnimalResponse converterParaResponse(Animal entidade) {
        AnimalDTO.AnimalResponse response = new AnimalDTO.AnimalResponse();
        response.setAnimalId(entidade.getAnimalId());
        response.setCodFazenda(entidade.getFazenda().getFazendaId());
        response.setCodLoteAtual(
                entidade.getLoteAtual() == null ? null : entidade.getLoteAtual().getLoteId());
        response.setIdentificacao(entidade.getIdentificacao());
        response.setEspecie(entidade.getEspecie());
        response.setRaca(entidade.getRaca());
        response.setPeso(entidade.getPeso());
        response.setIdadeMeses(entidade.getIdadeMeses());
        response.setDtaNascimento(entidade.getDtaNascimento());
        response.setDtaEntrada(entidade.getDtaEntrada());
        response.setObservacoes(entidade.getObservacoes());
        response.setAtivo(entidade.isAtivo());
        return response;
    }

    private void aplicarRequestNaEntidade(Animal entidade, AnimalDTO.AnimalRequest request) {
        entidade.setLoteAtual(
                request.getCodLoteAtual() == null
                        ? null
                        : loteRepository
                                .findById(request.getCodLoteAtual())
                                .filter(
                                        e ->
                                                e.getFazenda()
                                                        .getFazendaId()
                                                        .equals(contexto.codFazenda()))
                                .orElseThrow(() -> erro("Lote inexistente ou de outra fazenda.")));
        entidade.setIdentificacao(request.getIdentificacao().trim());
        entidade.setEspecie(request.getEspecie().trim());
        entidade.setRaca(request.getRaca().trim());
        entidade.setPeso(request.getPeso());
        entidade.setIdadeMeses(request.getIdadeMeses());
        entidade.setDtaNascimento(request.getDtaNascimento());
        entidade.setDtaEntrada(request.getDtaEntrada());
        entidade.setObservacoes(request.getObservacoes());
        if (!entidade.getLoteAtual().isAtivo()
                || entidade.getDtaEntrada().isBefore(entidade.getLoteAtual().getDtaFormacao()))
            throw erro("Data de entrada anterior à formação do lote ou lote inativo.");
        if (entidade.getDtaNascimento() != null
                && entidade.getDtaNascimento().isAfter(entidade.getDtaEntrada()))
            throw erro("Nascimento posterior à entrada.");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PROPRIETARIO','GERENTE_CAMPO')")
    public AnimalDTO.AnimalResponse transferir(
            Long id, TransferenciaDTO.TransferenciaRequest request) {
        contexto.bloquearFazenda();
        Animal animal = buscarEntidadePorId(id);
        Lote destino =
                loteRepository
                        .findByLoteIdAndFazendaFazendaId(
                                request.getCodLoteDestino(), contexto.codFazenda())
                        .orElseThrow(() -> erro("Lote de destino inválido."));
        var atual =
                vinculoAnimalLoteRepository
                        .findAllByFazendaFazendaId(contexto.codFazenda())
                        .stream()
                        .filter(
                                v ->
                                        v.getAnimal().getAnimalId().equals(id)
                                                && v.getDtaFim() == null)
                        .findFirst()
                        .orElseThrow();
        LocalDate data = request.getDtaOperacao();
        if (destino.getLoteId().equals(atual.getLote().getLoteId())
                || !destino.isAtivo()
                || !data.isAfter(atual.getDtaInicio())
                || data.isBefore(destino.getDtaFormacao())
                || data.isAfter(LocalDate.now()))
            throw erro("Destino ou data incompatível com o histórico do animal.");
        boolean posterior =
                pesagemRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .anyMatch(
                                p ->
                                        p.getLote() != null
                                                && p.getLote()
                                                        .getLoteId()
                                                        .equals(atual.getLote().getLoteId())
                                                && !p.getDtaPesagem().isBefore(data));
        posterior |=
                aplicacaoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .anyMatch(
                                a ->
                                        a.getLote() != null
                                                && a.getLote()
                                                        .getLoteId()
                                                        .equals(atual.getLote().getLoteId())
                                                && !a.getDtaOperacao().isBefore(data));
        posterior |=
                producaoPecuariaRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .anyMatch(
                                p ->
                                        p.getLote() != null
                                                && p.getLote()
                                                        .getLoteId()
                                                        .equals(atual.getLote().getLoteId())
                                                && !p.getDtaOperacao().isBefore(data));
        if (posterior)
            throw erro(
                    "Transferência retroativa alteraria a composição de operações já registradas.");
        atual.setDtaFim(data.minusDays(1));
        vinculoAnimalLoteRepository.saveAndFlush(atual);
        VinculoAnimalLote novo = new VinculoAnimalLote();
        novo.setFazenda(animal.getFazenda());
        novo.setAnimal(animal);
        novo.setLote(destino);
        novo.setDtaInicio(data);
        vinculoAnimalLoteRepository.save(novo);
        animal.setLoteAtual(destino);
        auditoriaService.registrar("TRANSFERENCIA", "Animal", id);
        return converterParaResponse(animal);
    }
}
