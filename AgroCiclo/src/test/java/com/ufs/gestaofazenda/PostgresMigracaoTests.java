package com.ufs.gestaofazenda;

import static org.assertj.core.api.Assertions.*;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

@EnabledOnOs(OS.LINUX)
class PostgresMigracaoTests {
    @Test
    void migraBancoAnteriorPreservandoDadosEValidaEsquemaAtual() throws Exception {
        try (var pg =
                EmbeddedPostgres.builder()
                        .setPort(0)
                        .setServerConfig("unix_socket_directories", "")
                        .start()) {
            var ds = pg.getPostgresDatabase();
            Flyway.configure().dataSource(ds).target("2").load().migrate();
            try (var c = ds.getConnection();
                    var s = c.createStatement()) {
                s.execute(
                        "INSERT INTO propriedades(id,nome,area_hectares,localizacao,responsavel)"
                            + " VALUES(1000,'Fazenda Antiga',200,'Sergipe','Responsável')");
                s.execute(
                        "INSERT INTO"
                            + " cargos(id,propriedade_id,nome,atribuicoes,custo_hora_referencia)"
                            + " VALUES(1000,1000,'Dono','Administrar',0)");
                s.execute(
                        "INSERT INTO"
                            + " funcionarios(id,propriedade_id,cargo_id,nome,horario_trabalho,custo_hora_efetivo)"
                            + " VALUES(1000,1000,1000,'Dono','Dia',0)");
                s.execute(
                        "INSERT INTO usuarios(id,funcionario_id,login,senha_hash,perfil)"
                            + " VALUES(1000,1000,'legado','"
                                + new org.springframework.security.crypto.bcrypt
                                                .BCryptPasswordEncoder()
                                        .encode("Senha-Segura-123!")
                                + "','PROPRIETARIO')");
            }
            Flyway.configure().dataSource(ds).load().migrate();
            try (var c = ds.getConnection();
                    var s = c.createStatement();
                    var rs =
                            s.executeQuery(
                                    "SELECT f.nome FROM usuarios_fazendas uf JOIN propriedades f ON"
                                        + " f.id=uf.cod_fazenda WHERE uf.cod_usuario=1000")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("Fazenda Antiga");
            }
            // Startup uses Hibernate validate, so missing or incompatible entity columns fail this
            // test.
            try (var app =
                    new org.springframework.boot.builder.SpringApplicationBuilder(
                                    GestaofazendaApplication.class)
                            .properties("spring.main.banner-mode=off")
                            .run(
                                    "--spring.datasource.url="
                                            + pg.getJdbcUrl("postgres", "postgres"),
                                    "--spring.datasource.username=postgres",
                                    "--spring.datasource.password=postgres",
                                    "--spring.jpa.hibernate.ddl-auto=validate",
                                    "--spring.main.web-application-type=servlet",
                                    "--server.port=0")) {
                assertThat(app.isActive()).isTrue();
                conferirConcorrencia(app);
            }
        }
    }

    private void conferirConcorrencia(
            org.springframework.context.ConfigurableApplicationContext app) throws Exception {
        int porta =
                ((org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext)
                                app)
                        .getWebServer()
                        .getPort();
        var cliente =
                java.net.http.HttpClient.newBuilder()
                        .cookieHandler(new java.net.CookieManager())
                        .build();
        String base = "http://localhost:" + porta;
        var login =
                java.net.http.HttpRequest.newBuilder(java.net.URI.create(base + "/api/auth/login"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(
                                java.net.http.HttpRequest.BodyPublishers.ofString(
                                        "login=legado&senha=Senha-Segura-123!"))
                        .build();
        assertThat(
                        cliente.send(login, java.net.http.HttpResponse.BodyHandlers.ofString())
                                .statusCode())
                .isEqualTo(302);
        var mapper = app.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
        var csrf =
                cliente.send(
                        java.net.http.HttpRequest.newBuilder(
                                        java.net.URI.create(base + "/api/csrf"))
                                .GET()
                                .build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
        String token = mapper.readTree(csrf.body()).get("token").asText();
        var item =
                postar(
                        cliente,
                        base,
                        token,
                        "/api/estoque/itens",
                        "item",
                        "{\"nome\":\"Teste"
                            + " concorrente\",\"categoria\":\"RACAO\",\"unidadeMedida\":\"kg\",\"estoqueMinimo\":0}");
        assertThat(item.statusCode()).describedAs(item.body()).isEqualTo(201);
        long codItem = mapper.readTree(item.body()).get("itemEstoqueId").asLong();
        String hoje = java.time.LocalDate.now().toString();
        var partida =
                postar(
                        cliente,
                        base,
                        token,
                        "/api/estoque/partidas",
                        "entrada",
                        "{\"codItem\":"
                                + codItem
                                + ",\"dtaEntrada\":\""
                                + hoje
                                + "\",\"quantidadeInicial\":10,\"valorUnitarioOrigem\":1,\"unidade\":\"kg\",\"referenciaOrigem\":\"ENT-1\"}");
        assertThat(partida.statusCode()).describedAs(partida.body()).isEqualTo(201);
        long codPartida = mapper.readTree(partida.body()).get("partidaEstoqueId").asLong();
        String movimento =
                "{\"codPartida\":"
                        + codPartida
                        + ",\"tipo\":\"DESCARTE\",\"quantidade\":7,\"dtaOperacao\":\""
                        + hoje
                        + "\",\"motivo\":\"Quebra\",\"unidade\":\"kg\",\"referenciaOrigem\":\"SAIDA-";
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            var primeiro =
                    executor.submit(
                            () ->
                                    postar(
                                            cliente,
                                            base,
                                            token,
                                            "/api/estoque/movimentacoes",
                                            "saida-1",
                                            movimento + "1\"}"));
            var segundo =
                    executor.submit(
                            () ->
                                    postar(
                                            cliente,
                                            base,
                                            token,
                                            "/api/estoque/movimentacoes",
                                            "saida-2",
                                            movimento + "2\"}"));
            assertThat(java.util.List.of(primeiro.get().statusCode(), segundo.get().statusCode()))
                    .containsExactlyInAnyOrder(201, 400);
        }
        var saldo =
                cliente.send(
                        java.net.http.HttpRequest.newBuilder(
                                        java.net.URI.create(
                                                base + "/api/estoque/partidas/" + codPartida))
                                .GET()
                                .build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(mapper.readTree(saldo.body()).get("quantidadeAtual").decimalValue())
                .isEqualByComparingTo("3");
    }

    private java.net.http.HttpResponse<String> postar(
            java.net.http.HttpClient cliente,
            String base,
            String token,
            String caminho,
            String chave,
            String json)
            throws Exception {
        var request =
                java.net.http.HttpRequest.newBuilder(java.net.URI.create(base + caminho))
                        .header("Content-Type", "application/json")
                        .header("X-XSRF-TOKEN", token)
                        .header("Idempotency-Key", chave)
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();
        return cliente.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
    }
}
