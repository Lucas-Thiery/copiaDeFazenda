package com.ufs.gestaofazenda;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.ufs.gestaofazenda.DTO.*;
import com.ufs.gestaofazenda.Entity.*;
import com.ufs.gestaofazenda.Repository.*;
import com.ufs.gestaofazenda.Service.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FluxosIntegracaoTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired SetupService setupService;
    @Autowired UsuarioRepository usuarios;
    @Autowired FuncionarioRepository funcionarios;
    @Autowired PartidaEstoqueRepository partidas;
    @Autowired LancamentoRepository lancamentos;
    @Autowired CompraRepository compras;
    @Autowired VinculoAnimalLoteRepository vinculos;
    @Autowired TarefaRepository tarefas;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    Long codFazenda;
    Long codFuncionario;

    @BeforeEach
    void preparar() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
        var r = new SetupDTO.SetupRequest();
        r.setPropriedadeNome("Fazenda Teste");
        r.setAreaHectares(new BigDecimal("200"));
        r.setLocalizacao("Sergipe");
        r.setResponsavel("Responsável");
        r.setNomeProprietario("Dono");
        r.setLogin("dono");
        r.setSenha("Senha-Segura-123!");
        setupService.inicializar(r);
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "dono",
                                "n/a",
                                List.of(new SimpleGrantedAuthority("ROLE_PROPRIETARIO"))));
        var u = usuarios.findByLoginIgnoreCase("dono").orElseThrow();
        codFazenda = u.getFuncionario().getFazenda().getFazendaId();
        codFuncionario = u.getFuncionario().getFuncionarioId();
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    JsonNode enviar(String caminho, Object dados) throws Exception {
        return enviar(caminho, dados, UUID.randomUUID().toString());
    }

    JsonNode enviar(String caminho, Object dados, String chave) throws Exception {
        String corpo =
                mvc.perform(
                                post("/api/" + caminho)
                                        .with(user("dono").roles("PROPRIETARIO"))
                                        .with(csrf())
                                        .header("Idempotency-Key", chave)
                                        .contentType("application/json")
                                        .content(mapper.writeValueAsBytes(dados)))
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return corpo.isBlank() ? mapper.nullNode() : mapper.readTree(corpo);
    }

    Map<String, Object> item() {
        return new LinkedHashMap<>(
                Map.of(
                        "nome",
                        "Ração",
                        "categoria",
                        "RACAO",
                        "unidadeMedida",
                        "kg",
                        "estoqueMinimo",
                        2));
    }

    long novoLote(String identificacao) throws Exception {
        long pasto =
                enviar(
                                "pastagens",
                                Map.of(
                                        "identificacao",
                                        "P-" + identificacao,
                                        "descricao",
                                        "Pasto físico"))
                        .get("pastagemId")
                        .asLong();
        return enviar(
                        "lotes",
                        Map.of(
                                "identificacao",
                                identificacao,
                                "codPastagemAtual",
                                pasto,
                                "dtaFormacao",
                                LocalDate.now().minusDays(30)))
                .get("loteId")
                .asLong();
    }

    long novaSafra() throws Exception {
        long talhao =
                enviar(
                                "talhoes",
                                Map.of(
                                        "identificacao",
                                        "T-1",
                                        "areaHectares",
                                        10,
                                        "descricaoLimites",
                                        "Cerca norte"))
                        .get("talhaoId")
                        .asLong();
        return enviar(
                        "safras",
                        Map.of(
                                "codTalhao",
                                talhao,
                                "identificacao",
                                "Milho-2026",
                                "cultura",
                                "Milho",
                                "areaPlantadaHa",
                                10,
                                "dtaInicio",
                                LocalDate.now().minusDays(20)))
                .get("safraId")
                .asLong();
    }

    long entrada(long item, String validade) throws Exception {
        return enviar(
                        "estoque/partidas",
                        Map.of(
                                "codItem",
                                item,
                                "dtaEntrada",
                                LocalDate.now().minusDays(15),
                                "dtaValidade",
                                validade,
                                "quantidadeInicial",
                                10,
                                "valorUnitarioOrigem",
                                3,
                                "unidade",
                                "kg",
                                "referenciaOrigem",
                                UUID.randomUUID().toString()))
                .get("partidaEstoqueId")
                .asLong();
    }

    @Test
    void setupProtegeSenhaEContaInicial() {
        var u = usuarios.findByLoginIgnoreCase("dono").orElseThrow();
        assertThat(encoder.matches("Senha-Segura-123!", u.getSenhaHash())).isTrue();
        assertThat(u.getFazendas()).hasSize(1);
        assertThatThrownBy(() -> setupService.inicializar(new SetupDTO.SetupRequest()))
                .hasMessageContaining("já foi concluída");
    }

    @Test
    void proprietarioCriaContaEAdministraVariasFazendasIsoladas() throws Exception {
        String email = "proprietaria@example.com";
        String senha = "Senha-Cadastro-123!";
        String cadastro = mapper.writeValueAsString(
                new CadastroProprietarioDTO("Ana da Silva", email, senha, true));
        mvc.perform(post("/api/cadastro/proprietario").with(anonymous()).with(csrf())
                        .contentType("application/json").content(cadastro))
                .andExpect(status().isCreated());

        var login = mvc.perform(post("/api/auth/login").with(anonymous())
                        .param("login", email).param("senha", senha))
                .andExpect(redirectedUrl("/fazendaweb/principal.html"))
                .andReturn();
        var sessao = (org.springframework.mock.web.MockHttpSession)
                login.getRequest().getSession(false);
        assertThat(sessao).isNotNull();

        mvc.perform(get("/api/sessao").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primeiraFazendaPendente").value(true));
        mvc.perform(get("/fazendaweb/principal.html").session(sessao))
                .andExpect(redirectedUrl("/fazendaweb/cadastroGerais/cadastrarPropriedade.html"));
        mvc.perform(get("/api/usuarios").session(sessao))
                .andExpect(status().isForbidden());

        String primeira = mapper.writeValueAsString(Map.of(
                "nome", "Fazenda da Serra", "areaHectares", 25,
                "localizacao", "Sergipe", "responsavel", "Ana da Silva"));
        var resultado = mvc.perform(post("/api/cadastro/primeira-fazenda")
                        .session(sessao).with(csrf()).contentType("application/json").content(primeira))
                .andExpect(status().isCreated()).andReturn();
        long primeiraId = mapper.readTree(resultado.getResponse().getContentAsString())
                .get("fazendaId").asLong();

        String segunda = mapper.writeValueAsString(Map.of(
                "nome", "Fazenda do Vale", "areaHectares", 35,
                "localizacao", "Bahia", "responsavel", "Ana da Silva"));
        var criada = mvc.perform(post("/api/fazendas").session(sessao).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType("application/json").content(segunda))
                .andExpect(status().isCreated()).andReturn();
        long segundaId = mapper.readTree(criada.getResponse().getContentAsString())
                .get("fazendaId").asLong();

        mvc.perform(get("/api/sessao").session(sessao).header("X-Fazenda-Id", segundaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codFazenda").value(segundaId))
                .andExpect(jsonPath("$.primeiraFazendaPendente").value(false));
        mvc.perform(get("/api/fazendas").session(sessao))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/sessao").session(sessao).header("X-Fazenda-Id", codFazenda))
                .andExpect(status().isForbidden());
        assertThat(usuarios.findByLoginIgnoreCase(email).orElseThrow().getFazendas())
                .extracting(Fazenda::getFazendaId).containsExactlyInAnyOrder(primeiraId, segundaId);
    }

    @Test
    void idsLongDtoPlanoNaoExpoeSenhaNemEntidadeRelacionada() throws Exception {
        var r =
                mvc.perform(get("/api/usuarios").with(user("dono").roles("PROPRIETARIO")))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(r)
                .contains("usuarioId", "codFuncionario", "codFazendas")
                .doesNotContain("senhaHash", "funcionario\":{");
        assertThat(Animal.class.getField("animalId").getType()).isEqualTo(Long.class);
    }

    @Test
    void impedeTalhaoMaiorQueFazendaEValidaTiposJakarta() throws Exception {
        mvc.perform(
                        post("/api/talhoes")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "area")
                                .contentType("application/json")
                                .content("{\"identificacao\":\"T\",\"areaHectares\":201}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/animais")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "invalid")
                                .contentType("application/json")
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reenvioRetornaMesmoResultadoSemNovaEntrada() throws Exception {
        var primeiro = enviar("estoque/itens", item(), "item-unico");
        var segundo = enviar("estoque/itens", item(), "item-unico");
        assertThat(segundo).isEqualTo(primeiro);
        var mudado = item();
        mudado.put("nome", "Outro");
        mvc.perform(
                        post("/api/estoque/itens")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "item-unico")
                                .contentType("application/json")
                                .content(mapper.writeValueAsBytes(mudado)))
                .andExpect(status().isConflict());
    }

    @Test
    void transferenciaPreservaVinculoEEventosAnteriores() throws Exception {
        long origem = novoLote("L-1"), destino = novoLote("L-2");
        long animal =
                enviar(
                                "animais",
                                Map.of(
                                        "identificacao",
                                        "BR-1",
                                        "especie",
                                        "Bovino",
                                        "raca",
                                        "Nelore",
                                        "peso",
                                        180,
                                        "dtaEntrada",
                                        LocalDate.now().minusDays(10),
                                        "codLoteAtual",
                                        origem))
                        .get("animalId")
                        .asLong();
        enviar(
                "animais/" + animal + "/transferencias",
                Map.of("codLoteDestino", destino, "dtaOperacao", LocalDate.now()));
        var historico =
                vinculos.findAll().stream()
                        .filter(v -> v.getAnimal().getAnimalId().equals(animal))
                        .toList();
        assertThat(historico).hasSize(2);
        assertThat(historico)
                .anySatisfy(
                        v -> {
                            assertThat(v.getLote().getLoteId()).isEqualTo(origem);
                            assertThat(v.getDtaFim()).isEqualTo(LocalDate.now().minusDays(1));
                        });
        assertThat(historico)
                .anySatisfy(
                        v -> {
                            assertThat(v.getLote().getLoteId()).isEqualTo(destino);
                            assertThat(v.getDtaFim()).isNull();
                        });
    }

    @Test
    void vencidoBloqueiaUsoMasPermiteDescarte() throws Exception {
        long item = enviar("estoque/itens", item()).get("itemEstoqueId").asLong();
        long partida = entrada(item, LocalDate.now().minusDays(1).toString());
        var dados =
                new LinkedHashMap<String, Object>(
                        Map.of(
                                "codPartida",
                                partida,
                                "tipo",
                                "USO",
                                "quantidade",
                                2,
                                "dtaOperacao",
                                LocalDate.now(),
                                "unidade",
                                "kg",
                                "motivo",
                                "Uso",
                                "referenciaOrigem",
                                "MOV-1",
                                "codLote",
                                novoLote("L-1")));
        mvc.perform(
                        post("/api/estoque/movimentacoes")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "vencido")
                                .contentType("application/json")
                                .content(mapper.writeValueAsBytes(dados)))
                .andExpect(status().isBadRequest());
        assertThat(partidas.findById(partida).orElseThrow().getQuantidadeAtual())
                .isEqualByComparingTo("10");
        dados.put("tipo", "DESCARTE");
        dados.remove("codLote");
        enviar("estoque/movimentacoes", dados);
        assertThat(partidas.findById(partida).orElseThrow().getQuantidadeAtual())
                .isEqualByComparingTo("8");
    }

    @Test
    void compraGeraEstoqueObrigacaoMasNaoCaixa() throws Exception {
        long fornecedor =
                enviar(
                                "fornecedores",
                                Map.of("identificacao", "F-1", "contato", "contato@exemplo.com"))
                        .get("fornecedorId")
                        .asLong();
        long item = enviar("estoque/itens", item()).get("itemEstoqueId").asLong();
        var compra =
                Map.of(
                        "codFornecedor",
                        fornecedor,
                        "referencia",
                        "NF-001",
                        "dtaOperacao",
                        LocalDate.now(),
                        "total",
                        30,
                        "itens",
                        List.of(
                                Map.of(
                                        "codItem",
                                        item,
                                        "quantidade",
                                        10,
                                        "unidade",
                                        "kg",
                                        "valorUnitario",
                                        3,
                                        "dtaValidade",
                                        LocalDate.now().plusDays(90))));
        enviar("compras", compra, "compra-1");
        enviar("compras", compra, "compra-1");
        assertThat(compras.count()).isEqualTo(1);
        assertThat(partidas.count()).isEqualTo(1);
        assertThat(lancamentos.count()).isEqualTo(1);
        var fluxo =
                mvc.perform(
                                get("/api/fluxo-caixa")
                                        .param("dtaInicio", LocalDate.now().minusDays(1).toString())
                                        .param("dtaFim", LocalDate.now().toString())
                                        .with(user("dono").roles("PROPRIETARIO")))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(mapper.readTree(fluxo).get("pagamentos").decimalValue())
                .isEqualByComparingTo("0");
        assertThat(mapper.readTree(fluxo).get("despesasPendentes").decimalValue())
                .isEqualByComparingTo("30");
    }

    @Test
    void liquidacaoParcialAtualizaSaldoERejeitaExcesso() throws Exception {
        long id =
                enviar(
                                "lancamentos",
                                Map.of(
                                        "tipo",
                                        "DESPESA",
                                        "dtaOperacao",
                                        LocalDate.now(),
                                        "descricao",
                                        "Serviço",
                                        "categoria",
                                        "GERAL",
                                        "referencia",
                                        "DOC-1",
                                        "emitente",
                                        "Pessoa",
                                        "valor",
                                        100))
                        .get("lancamentoId")
                        .asLong();
        enviar(
                "liquidacoes",
                Map.of(
                        "codLancamento",
                        id,
                        "dtaOperacao",
                        LocalDate.now(),
                        "valor",
                        30,
                        "comprovante",
                        "PIX-1"));
        assertThat(lancamentos.findById(id).orElseThrow().getValorLiquidado())
                .isEqualByComparingTo("30");
        mvc.perform(
                        post("/api/liquidacoes")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "excesso")
                                .contentType("application/json")
                                .content(
                                        mapper.writeValueAsBytes(
                                                Map.of(
                                                        "codLancamento",
                                                        id,
                                                        "dtaOperacao",
                                                        LocalDate.now(),
                                                        "valor",
                                                        80,
                                                        "comprovante",
                                                        "PIX-2"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void etapasColheitaNaoPermitemPularOuEncerrarSemProducao() throws Exception {
        long safra = novaSafra();
        mvc.perform(
                        post("/api/etapas-cultivo")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "pular")
                                .contentType("application/json")
                                .content(
                                        mapper.writeValueAsBytes(
                                                Map.of(
                                                        "codSafra",
                                                        safra,
                                                        "etapa",
                                                        "COLHEITA",
                                                        "dtaOperacao",
                                                        LocalDate.now()))))
                .andExpect(status().isBadRequest());
        for (String etapa : List.of("PREPARO", "PLANTIO", "COLHEITA"))
            enviar(
                    "etapas-cultivo",
                    Map.of("codSafra", safra, "etapa", etapa, "dtaOperacao", LocalDate.now()));
        enviar(
                "producoes-colheita",
                Map.of(
                        "codSafra",
                        safra,
                        "dtaOperacao",
                        LocalDate.now(),
                        "quantidade",
                        20,
                        "unidade",
                        "kg"));
        enviar(
                "etapas-cultivo",
                Map.of("codSafra", safra, "etapa", "ENCERRAMENTO", "dtaOperacao", LocalDate.now()));
    }

    @Test
    void restricaoFinanceiroOperadorEOutraFazenda() throws Exception {
        var dono = usuarios.findByLoginIgnoreCase("dono").orElseThrow();
        Funcionario funcionario = new Funcionario();
        funcionario.setFazenda(dono.getFuncionario().getFazenda());
        funcionario.setCargo(dono.getFuncionario().getCargo());
        funcionario.setNome("Operador");
        funcionario.setHorarioTrabalho("Dia");
        funcionario.setCustoHoraEfetivo(BigDecimal.TEN);
        funcionarios.save(funcionario);
        Usuario operador = new Usuario();
        operador.setFuncionario(funcionario);
        operador.setLogin("operador");
        operador.setSenhaHash(encoder.encode("Senha-Segura-123!"));
        operador.setPerfil(Perfil.OPERADOR);
        operador.getFazendas().addAll(dono.getFazendas());
        usuarios.save(operador);
        mvc.perform(get("/api/lancamentos").with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        get("/api/animais")
                                .header("X-Fazenda-Id", "999999")
                                .with(user("dono").roles("PROPRIETARIO")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/estoque/itens")
                                .with(user("operador").roles("OPERADOR"))
                                .with(csrf())
                                .header("Idempotency-Key", "negado")
                                .contentType("application/json")
                                .content(mapper.writeValueAsBytes(item())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ultimoProprietarioNaoPodeSerDesativado() throws Exception {
        long id = usuarios.findByLoginIgnoreCase("dono").orElseThrow().getUsuarioId();
        mvc.perform(
                        patch("/api/usuarios/" + id + "/desativacao")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "ultimo"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aplicacaoBaixaUmaVezEPreservaCustoDaOrigem() throws Exception {
        long lote = novoLote("L-CONSUMO");
        long item = enviar("estoque/itens", item()).get("itemEstoqueId").asLong();
        long partida = entrada(item, LocalDate.now().plusDays(90).toString());
        var aplicacao =
                Map.of(
                        "codPartida",
                        partida,
                        "codLote",
                        lote,
                        "quantidade",
                        2,
                        "unidade",
                        "kg",
                        "dtaOperacao",
                        LocalDate.now(),
                        "codResponsavel",
                        codFuncionario,
                        "referencia",
                        "AP-1");
        enviar("aplicacoes", aplicacao, "aplicacao-1");
        enviar("aplicacoes", aplicacao, "aplicacao-1");
        assertThat(partidas.findById(partida).orElseThrow().getQuantidadeAtual())
                .isEqualByComparingTo("8");
        assertThat(lancamentos.count()).isZero();
        var resposta =
                mvc.perform(
                                get("/api/custos/LOTE/" + lote)
                                        .param(
                                                "dtaInicio",
                                                LocalDate.now().minusDays(10).toString())
                                        .param("dtaFim", LocalDate.now().toString())
                                        .with(user("dono").roles("PROPRIETARIO")))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(mapper.readTree(resposta).get("total").decimalValue()).isEqualByComparingTo("6");
    }

    @Test
    void tarefaPreservaTaxaDuracaoENaoRecalculaAposReenvio() throws Exception {
        long safra = novaSafra();
        long cargo =
                enviar(
                                "cargos",
                                Map.of(
                                        "nome",
                                        "Trabalhador",
                                        "atribuicoes",
                                        "Campo",
                                        "custoHoraReferencia",
                                        20))
                        .get("cargoId")
                        .asLong();
        long funcionario =
                enviar(
                                "funcionarios",
                                Map.of(
                                        "nome",
                                        "Operador",
                                        "codCargo",
                                        cargo,
                                        "horarioTrabalho",
                                        "Dia"))
                        .get("funcionarioId")
                        .asLong();
        enviar(
                "usuarios",
                Map.of(
                        "codFuncionario",
                        funcionario,
                        "login",
                        "operador",
                        "senha",
                        "Senha-Segura-123!",
                        "perfil",
                        "OPERADOR",
                        "codFazendas",
                        List.of(codFazenda)));
        var inicio = LocalDateTime.now().minusHours(3).withNano(0);
        var fim = inicio.plusHours(2);
        long tarefa =
                enviar(
                                "tarefas",
                                Map.of(
                                        "codExecutante",
                                        funcionario,
                                        "codSafra",
                                        safra,
                                        "atividade",
                                        "Manejo",
                                        "local",
                                        "Talhão",
                                        "dtaInicioPrevisto",
                                        inicio,
                                        "dtaFimPrevisto",
                                        fim))
                        .get("tarefaId")
                        .asLong();
        var conclusao = Map.of("dtaInicio", inicio, "dtaFim", fim);
        enviar("tarefas/" + tarefa + "/concluir", conclusao);
        funcionarios.findById(funcionario).orElseThrow().setCustoHoraEfetivo(new BigDecimal("100"));
        enviar("tarefas/" + tarefa + "/concluir", conclusao);
        assertThat(tarefas.findById(tarefa).orElseThrow().getCustoMaoObra())
                .isEqualByComparingTo("40");
        mvc.perform(get("/api/tarefas/" + tarefa).with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoMaoObra").isEmpty());
    }

    @Test
    void maquinaMantemUnidadeEImpedeLeituraRegressiva() throws Exception {
        long maquina =
                enviar(
                                "maquinas",
                                Map.of(
                                        "identificacao",
                                        "TR-1",
                                        "modelo",
                                        "Trator",
                                        "ano",
                                        2020,
                                        "abastecivel",
                                        true,
                                        "unidadeMedidor",
                                        "HORAS",
                                        "leitura",
                                        100))
                        .get("maquinaId")
                        .asLong();
        enviar(
                "usos-maquina",
                Map.of("codMaquina", maquina, "dtaOperacao", LocalDate.now(), "leitura", 110));
        mvc.perform(
                        post("/api/usos-maquina")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .header("Idempotency-Key", "regressiva")
                                .contentType("application/json")
                                .content(
                                        mapper.writeValueAsBytes(
                                                Map.of(
                                                        "codMaquina",
                                                        maquina,
                                                        "dtaOperacao",
                                                        LocalDate.now(),
                                                        "leitura",
                                                        105))))
                .andExpect(status().isBadRequest());
        enviar(
                "planos-manutencao",
                Map.of("codMaquina", maquina, "servico", "Revisão", "limiteMedidor", 120));
        enviar(
                "configuracoes-alerta",
                Map.of(
                        "diasVacina",
                        5,
                        "diasValidade",
                        7,
                        "diasManutencao",
                        3,
                        "margemHoras",
                        10,
                        "margemKm",
                        20));
        mvc.perform(get("/api/alertas").with(user("dono").roles("PROPRIETARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("MANUTENCAO"));
    }

    @Test
    void relatoriosUsamMesmosDadosParaJsonCsvEPdf() throws Exception {
        String inicio = LocalDate.now().minusDays(1).toString(), fim = LocalDate.now().toString();
        mvc.perform(
                        get("/api/relatorios/custos")
                                .param("dtaInicio", inicio)
                                .param("dtaFim", fim)
                                .param("formato", "csv")
                                .with(user("dono").roles("PROPRIETARIO")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
        byte[] pdf =
                mvc.perform(
                                get("/api/relatorios/custos")
                                        .param("dtaInicio", inicio)
                                        .param("dtaFim", fim)
                                        .param("formato", "pdf")
                                        .with(user("dono").roles("PROPRIETARIO")))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();
        assertThat(new String(pdf, java.nio.charset.StandardCharsets.ISO_8859_1))
                .startsWith("%PDF-1.4")
                .contains("%%EOF");
        java.nio.file.Path destino = java.nio.file.Path.of("target", "relatorio-amostra.pdf");
        java.nio.file.Files.write(destino, pdf);
    }

    @Test
    void agendamentoNaoBaixaEstoqueEAplicacaoSanitariaConcluiComCarencia() throws Exception {
        long lote = novoLote("SANIDADE");
        long item =
                enviar(
                                "estoque/itens",
                                Map.of(
                                        "nome",
                                        "Produto sanitário",
                                        "categoria",
                                        "VACINA",
                                        "unidadeMedida",
                                        "kg",
                                        "estoqueMinimo",
                                        0))
                        .get("itemEstoqueId")
                        .asLong();
        long partida = entrada(item, LocalDate.now().plusDays(90).toString());
        long calendario =
                enviar(
                                "calendarios-sanitarios",
                                Map.of(
                                        "codLote",
                                        lote,
                                        "codProduto",
                                        item,
                                        "acao",
                                        "Ação informada pelo técnico",
                                        "orientacaoTecnica",
                                        "Orientação fictícia apenas para teste",
                                        "dtaPrevista",
                                        LocalDate.now()))
                        .get("calendarioSanitarioId")
                        .asLong();
        long agendamento =
                enviar(
                                "agendamentos-sanitarios",
                                Map.of(
                                        "codCalendario",
                                        calendario,
                                        "codLote",
                                        lote,
                                        "codResponsavel",
                                        codFuncionario,
                                        "dtaPrevista",
                                        LocalDate.now()))
                        .get("agendamentoSanitarioId")
                        .asLong();
        assertThat(partidas.findById(partida).orElseThrow().getQuantidadeAtual())
                .isEqualByComparingTo("10");
        enviar(
                "aplicacoes",
                Map.of(
                        "codPartida",
                        partida,
                        "codLote",
                        lote,
                        "codAgendamento",
                        agendamento,
                        "codResponsavel",
                        codFuncionario,
                        "dtaOperacao",
                        LocalDate.now(),
                        "quantidade",
                        1,
                        "unidade",
                        "kg",
                        "dosagem",
                        "Registro do técnico",
                        "referencia",
                        "SAN-1",
                        "carenciaDias",
                        5));
        var producao =
                enviar(
                        "producoes-pecuarias",
                        Map.of(
                                "codLote",
                                lote,
                                "produto",
                                "Leite",
                                "dtaOperacao",
                                LocalDate.now(),
                                "quantidade",
                                10,
                                "unidade",
                                "L",
                                "destinacao",
                                "VENDA"));
        assertThat(producao.get("avisoCarencia").asText()).contains("pendentes");
        assertThat(partidas.findById(partida).orElseThrow().getQuantidadeAtual())
                .isEqualByComparingTo("9");
    }

    @Test
    void rejeitaGravacaoSemChaveOuSemCsrf() throws Exception {
        mvc.perform(
                        post("/api/estoque/itens")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(mapper.writeValueAsBytes(item())))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/estoque/itens")
                                .with(user("dono").roles("PROPRIETARIO"))
                                .header("Idempotency-Key", "csrf")
                                .contentType("application/json")
                                .content(mapper.writeValueAsBytes(item())))
                .andExpect(status().isForbidden());
    }

    @Test
    void contaDesativadaNaoPodeUsarSessaoAnterior() throws Exception {
        usuarios.findByLoginIgnoreCase("dono").orElseThrow().setAtivo(false);
        mvc.perform(get("/api/animais").with(user("dono").roles("PROPRIETARIO")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void novoCicloEPrimeiroPreparoSaoRegistradosJuntos() throws Exception {
        long talhao =
                enviar("talhoes", Map.of("identificacao", "NOVO-CICLO", "areaHectares", 8))
                        .get("talhaoId")
                        .asLong();
        var etapa =
                enviar(
                        "etapas-cultivo",
                        Map.of(
                                "etapa",
                                "PREPARO",
                                "dtaOperacao",
                                LocalDate.now(),
                                "novaSafra",
                                Map.of(
                                        "codTalhao",
                                        talhao,
                                        "identificacao",
                                        "CICLO-NOVO",
                                        "cultura",
                                        "Milho",
                                        "areaPlantadaHa",
                                        8,
                                        "dtaInicio",
                                        LocalDate.now())));
        assertThat(etapa.get("codSafra").asLong()).isPositive();
    }
}
