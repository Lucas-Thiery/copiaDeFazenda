package com.ufs.gestaofazenda.Service;

import com.ufs.gestaofazenda.DTO.RelatorioDTO;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

@Service
public class ExportacaoService {
    public byte[] csv(RelatorioDTO.RelatorioResponse relatorio) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        registro(
                csv,
                List.of(
                        "tipo",
                        relatorio.getTipo(),
                        "codFazenda",
                        relatorio.getCodFazenda().toString(),
                        "dtaInicio",
                        relatorio.getDtaInicio().toString(),
                        "dtaFim",
                        relatorio.getDtaFim().toString()));
        registro(csv, List.of("filtros", relatorio.getFiltros().toString()));
        registro(csv, relatorio.getColunas());
        relatorio.getLinhas().forEach(l -> registro(csv, l));
        relatorio.getLimitacoes().forEach(l -> registro(csv, List.of("Limitação", l)));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void registro(StringBuilder destino, List<String> campos) {
        StringJoiner linha = new StringJoiner(";");
        for (String campo : campos) {
            String valor = campo == null ? "" : campo;
            if (valor.matches("^[=+@\\-].*") && !valor.matches("-?\\d+(\\.\\d+)?"))
                valor = "'" + valor;
            linha.add("\"" + valor.replace("\"", "\"\"") + "\"");
        }
        destino.append(linha).append("\r\n");
    }

    // A small text-only PDF: paginated rows, repeated headers, standard WinAnsi fonts.
    // It intentionally shares the report data with CSV and JSON.
    public byte[] pdf(RelatorioDTO.RelatorioResponse relatorio) {
        List<String> linhas = new ArrayList<>();
        linhas.add("AgroCiclo - " + relatorio.getTipo());
        linhas.add(
                "Fazenda "
                        + relatorio.getCodFazenda()
                        + " | "
                        + relatorio.getDtaInicio()
                        + " a "
                        + relatorio.getDtaFim());
        linhas.add("Filtros: " + relatorio.getFiltros());
        linhas.add("");
        for (List<String> registro : relatorio.getLinhas()) {
            for (int i = 0; i < registro.size(); i++) {
                String texto = relatorio.getColunas().get(i) + ": " + registro.get(i);
                for (int pos = 0; pos < texto.length(); pos += 100)
                    linhas.add(texto.substring(pos, Math.min(pos + 100, texto.length())));
            }
            linhas.add("");
        }
        for (String limite : relatorio.getLimitacoes()) {
            String texto = "Limitação: " + limite;
            for (int pos = 0; pos < texto.length(); pos += 100)
                linhas.add(texto.substring(pos, Math.min(pos + 100, texto.length())));
        }
        int paginas = Math.max(1, (linhas.size() + 49) / 50);
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        StringJoiner filhos = new StringJoiner(" ");
        for (int p = 0; p < paginas; p++) filhos.add((4 + 2 * p) + " 0 R");
        objetos.add(bytes("<< /Type /Pages /Count " + paginas + " /Kids [" + filhos + "] >>"));
        objetos.add(
                bytes(
                        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding"
                            + " /WinAnsiEncoding >>"));
        for (int p = 0; p < paginas; p++) {
            objetos.add(
                    bytes(
                            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources <<"
                                + " /Font << /F1 3 0 R >> >> /Contents "
                                    + (5 + 2 * p)
                                    + " 0 R >>"));
            StringBuilder texto = new StringBuilder("BT /F1 9 Tf 40 800 Td 14 TL ");
            texto.append("(AgroCiclo - página ")
                    .append(p + 1)
                    .append(" de ")
                    .append(paginas)
                    .append(") Tj T* ");
            for (int l = p * 50; l < Math.min((p + 1) * 50, linhas.size()); l++) {
                texto.append('(')
                        .append(
                                linhas.get(l)
                                        .replace("\\", "\\\\")
                                        .replace("(", "\\(")
                                        .replace(")", "\\)")
                                        .replace("\r", " ")
                                        .replace("\n", " "))
                        .append(") Tj T* ");
            }
            byte[] stream = bytes(texto.append("ET").toString());
            ByteArrayOutputStream conteudo = new ByteArrayOutputStream();
            conteudo.writeBytes(bytes("<< /Length " + stream.length + " >>\nstream\n"));
            conteudo.writeBytes(stream);
            conteudo.writeBytes(bytes("\nendstream"));
            objetos.add(conteudo.toByteArray());
        }
        ByteArrayOutputStream arquivo = new ByteArrayOutputStream();
        arquivo.writeBytes(bytes("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(arquivo.size());
            arquivo.writeBytes(bytes((i + 1) + " 0 obj\n"));
            arquivo.writeBytes(objetos.get(i));
            arquivo.writeBytes(bytes("\nendobj\n"));
        }
        int xref = arquivo.size();
        arquivo.writeBytes(bytes("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++)
            arquivo.writeBytes(
                    bytes(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
        arquivo.writeBytes(
                bytes(
                        "trailer\n<< /Size "
                                + (objetos.size() + 1)
                                + " /Root 1 0 R >>\nstartxref\n"
                                + xref
                                + "\n%%EOF\n"));
        return arquivo.toByteArray();
    }

    private byte[] bytes(String texto) {
        return texto.getBytes(Charset.forName("windows-1252"));
    }
}
