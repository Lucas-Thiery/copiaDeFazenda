# Testes executados

Data: 07/11/2026.

Execução final: **20 testes, 0 falhas, 0 erros, 0 ignorados**. `clean verify` finalizado com código 0.

Java: JDK 21 completo. Spring Boot 3.5.7. Flyway 11.15.0. H2 nos testes rápidos e PostgreSQL 18.1 real, temporário, no teste de migrations.

| Classe | Cenário | Resultado |
|---|---|---|
| FluxosIntegracaoTests | `ultimoProprietarioNaoPodeSerDesativado` | aprovado |
| FluxosIntegracaoTests | `novoCicloEPrimeiroPreparoSaoRegistradosJuntos` | aprovado |
| FluxosIntegracaoTests | `compraGeraEstoqueObrigacaoMasNaoCaixa` | aprovado |
| FluxosIntegracaoTests | `maquinaMantemUnidadeEImpedeLeituraRegressiva` | aprovado |
| FluxosIntegracaoTests | `liquidacaoParcialAtualizaSaldoERejeitaExcesso` | aprovado |
| FluxosIntegracaoTests | `relatoriosUsamMesmosDadosParaJsonCsvEPdf` | aprovado |
| FluxosIntegracaoTests | `contaDesativadaNaoPodeUsarSessaoAnterior` | aprovado |
| FluxosIntegracaoTests | `rejeitaGravacaoSemChaveOuSemCsrf` | aprovado |
| FluxosIntegracaoTests | `aplicacaoBaixaUmaVezEPreservaCustoDaOrigem` | aprovado |
| FluxosIntegracaoTests | `tarefaPreservaTaxaDuracaoENaoRecalculaAposReenvio` | aprovado |
| FluxosIntegracaoTests | `impedeTalhaoMaiorQueFazendaEValidaTiposJakarta` | aprovado |
| FluxosIntegracaoTests | `agendamentoNaoBaixaEstoqueEAplicacaoSanitariaConcluiComCarencia` | aprovado |
| FluxosIntegracaoTests | `idsLongDtoPlanoNaoExpoeSenhaNemEntidadeRelacionada` | aprovado |
| FluxosIntegracaoTests | `transferenciaPreservaVinculoEEventosAnteriores` | aprovado |
| FluxosIntegracaoTests | `reenvioRetornaMesmoResultadoSemNovaEntrada` | aprovado |
| FluxosIntegracaoTests | `vencidoBloqueiaUsoMasPermiteDescarte` | aprovado |
| FluxosIntegracaoTests | `restricaoFinanceiroOperadorEOutraFazenda` | aprovado |
| FluxosIntegracaoTests | `etapasColheitaNaoPermitemPularOuEncerrarSemProducao` | aprovado |
| FluxosIntegracaoTests | `setupProtegeSenhaEContaInicial` | aprovado |
| PostgresMigracaoTests | `migraBancoAnteriorPreservandoDadosEValidaEsquemaAtual` | aprovado |

O teste PostgreSQL também faz login HTTP real e envia duas saídas incompatíveis simultaneamente. Uma retorna 201, outra 400, e o saldo final é conferido como 3 a partir de uma partida com 10 e duas solicitações de 7.

A verificação das migrations começa no esquema V2 com dados legados. Após a evolução, a relação N–N usuário–fazenda preserva o vínculo antigo e a aplicação inicia com Hibernate `validate`. Isso não substitui validar uma cópia da base real.

A exportação PDF de amostra foi aberta com Poppler para extração e renderização. Os JavaScripts de integração alterados passaram em `node --check`. Não houve teste de navegação completo das telas.

Os logs/XML em `evidencias/` contêm dados fictícios dos testes. Nenhuma senha de usuário é retornada pelas APIs. Os testes iniciais que encontraram problemas de ambiente/compilação não são tratados como aprovados; a contagem acima é da execução final.

Não foram aferidos: cobertura de 70% das regras, SUS, UAT, desempenho em dois segundos, vulnerabilidades de dependências, entrega/leitura de e-mail e rotinas de backup/restauração de produção.
