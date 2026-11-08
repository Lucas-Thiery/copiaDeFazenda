package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RebanhoService {
    private final VinculoAnimalLoteRepository vinculoAnimalLoteRepository;
    private final ContextoUsuario contexto;

    public Lote loteNaData(Animal animal, LocalDate data) {
        return vinculoAnimalLoteRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .filter(
                        v ->
                                v.getAnimal().getAnimalId().equals(animal.getAnimalId())
                                        && !data.isBefore(v.getDtaInicio())
                                        && (v.getDtaFim() == null || !data.isAfter(v.getDtaFim())))
                .map(VinculoAnimalLote::getLote)
                .findFirst()
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Animal sem vínculo histórico na data informada."));
    }

    public List<Long> composicao(Lote lote, LocalDate data) {
        return vinculoAnimalLoteRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                .filter(
                        v ->
                                v.getLote().getLoteId().equals(lote.getLoteId())
                                        && !data.isBefore(v.getDtaInicio())
                                        && (v.getDtaFim() == null || !data.isAfter(v.getDtaFim())))
                .map(v -> v.getAnimal().getAnimalId())
                .sorted()
                .toList();
    }
}
