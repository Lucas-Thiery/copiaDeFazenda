package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.Config.ContextoUsuario;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.AplicacaoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarenciaService {
    private final AplicacaoRepository aplicacaoRepository;
    private final ContextoUsuario contexto;

    public String aviso(Safra safra, Lote lote, LocalDate data) {
        var aplicacoes =
                aplicacaoRepository.findAllByFazendaFazendaId(contexto.codFazenda()).stream()
                        .filter(a -> !a.getDtaOperacao().isAfter(data))
                        .filter(
                                a ->
                                        (safra != null
                                                        && a.getSafra() != null
                                                        && a.getSafra()
                                                                .getSafraId()
                                                                .equals(safra.getSafraId()))
                                                || (lote != null
                                                        && a.getLote() != null
                                                        && a.getLote()
                                                                .getLoteId()
                                                                .equals(lote.getLoteId())))
                        .toList();
        if (aplicacoes.isEmpty())
            return "Referências técnicas de carência não disponíveis; liberação não verificada.";
        for (var a : aplicacoes) {
            if (a.getCarenciaDias() == null
                    || a.getOrientacaoTecnica() == null
                    || a.getOrientacaoTecnica().isBlank())
                return "Parâmetros técnicos pendentes; liberação não verificada.";
            if (data.isBefore(a.getDtaOperacao().plusDays(a.getCarenciaDias())))
                return "Produto dentro do período de carência registrado.";
        }
        return "Nenhuma carência pendente nas referências registradas; conferir orientação"
                   + " técnica.";
    }
}
