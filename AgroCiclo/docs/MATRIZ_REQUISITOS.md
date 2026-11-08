# Rastreabilidade dos requisitos

A coluna de situação descreve o backend. **Nenhum RF está marcado como produto integralmente homologado.** Cenários citados demonstram apenas o comportamento testado, não todos os fluxos do requisito. As telas, a matriz completa de acessos e a aceitação ainda precisam de verificação.

| RF | Função do plano | Código | Situação e evidência |
|---|---|---|---|
| RF01 | Autenticar usuário | `SecurityConfig` | Backend com cenário aprovado: login HTTP PostgreSQL / BCrypt. |
| RF02 | Encerrar sessão | `SecurityConfig` | Backend implementado; teste específico/aceitação pendente. |
| RF03 | Restringir funções por perfil | `SecurityConfig / @PreAuthorize` | Backend com cenário aprovado: restricaoFinanceiroOperadorEOutraFazenda. |
| RF04 | Cadastrar propriedade | `FazendaService` | Backend implementado; teste específico/aceitação pendente. |
| RF05 | Cadastrar talhão | `TalhaoService` | Backend com cenário aprovado: impedeTalhaoMaiorQueFazendaEValidaTiposJakarta. |
| RF06 | Cadastrar cargo | `CargoService` | Backend implementado; teste específico/aceitação pendente. |
| RF07 | Cadastrar funcionário | `FuncionarioService` | Backend com cenário aprovado: tarefaPreservaTaxaDuracaoENaoRecalculaAposReenvio. |
| RF08 | Cadastrar usuário | `UsuarioService` | Backend com cenário aprovado: idsLongDtoPlanoNaoExpoeSenhaNemEntidadeRelacionada / criação de Operador. |
| RF09 | Atribuir perfil ao usuário | `UsuarioService` | Backend com cenário aprovado: criação de Operador. |
| RF10 | Desativar usuário | `UsuarioService` | Backend com cenário aprovado: ultimoProprietarioNaoPodeSerDesativado / contaDesativadaNaoPodeUsarSessaoAnterior. |
| RF11 | Registrar trilha de auditoria | `AuditoriaService` | Backend implementado; teste específico/aceitação pendente. |
| RF12 | Cadastrar animal | `AnimalService` | Backend com cenário aprovado: transferenciaPreservaVinculoEEventosAnteriores. |
| RF13 | Cadastrar lote de animais | `LoteService` | Backend com cenário aprovado: lotes usados nas fixtures HTTP. |
| RF14 | Cadastrar pastagem | `PastagemService` | Backend com cenário aprovado: pastagens usadas nas fixtures HTTP. |
| RF15 | Movimentar animais entre lotes | `AnimalService.transferir` | Backend com cenário aprovado: transferenciaPreservaVinculoEEventosAnteriores. |
| RF16 | Registrar pesagem | `PesagemService` | Backend implementado; teste específico/aceitação pendente. |
| RF17 | Consultar evolução do peso | `PesagemService.evolucao` | Backend implementado; teste específico/aceitação pendente. |
| RF18 | Registrar vacinação | `AplicacaoService` | Backend com cenário aprovado: agendamentoNaoBaixaEstoqueEAplicacaoSanitariaConcluiComCarencia. |
| RF19 | Registrar tratamento sanitário | `AplicacaoService` | Backend implementado; teste específico/aceitação pendente. |
| RF20 | Registrar produção animal | `ProducaoPecuariaService` | Backend com cenário aprovado: agendamentoNaoBaixaEstoqueEAplicacaoSanitariaConcluiComCarencia. |
| RF21 | Planejar safra | `SafraService / PlanoManejoService` | Backend implementado; teste específico/aceitação pendente. |
| RF22 | Registrar plantio | `EtapaCultivoService` | Backend com cenário aprovado: novoCicloEPrimeiroPreparoSaoRegistradosJuntos. |
| RF23 | Registrar manejo da lavoura | `ManejoService` | Backend implementado; teste específico/aceitação pendente. |
| RF24 | Registrar aplicação de insumos | `AplicacaoService` | Backend com cenário aprovado: aplicacaoBaixaUmaVezEPreservaCustoDaOrigem. |
| RF25 | Baixar insumos aplicados do estoque | `MovimentacaoEstoqueService` | Backend com cenário aprovado: aplicacaoBaixaUmaVezEPreservaCustoDaOrigem. |
| RF26 | Registrar colheita | `ProducaoColheitaService` | Backend com cenário aprovado: etapasColheitaNaoPermitemPularOuEncerrarSemProducao. |
| RF27 | Cadastrar item de estoque | `ItemEstoqueService` | Backend com cenário aprovado: reenvioRetornaMesmoResultadoSemNovaEntrada. |
| RF28 | Cadastrar fornecedor | `FornecedorService` | Backend com cenário aprovado: compraGeraEstoqueObrigacaoMasNaoCaixa. |
| RF29 | Registrar compra | `CompraService` | Backend com cenário aprovado: compraGeraEstoqueObrigacaoMasNaoCaixa. |
| RF30 | Registrar entrada no estoque | `PartidaEstoqueService` | Backend com cenário aprovado: compraGeraEstoqueObrigacaoMasNaoCaixa. |
| RF31 | Registrar saída do estoque | `MovimentacaoEstoqueService` | Backend com cenário aprovado: vencidoBloqueiaUsoMasPermiteDescarte / concorrência PostgreSQL. |
| RF32 | Registrar receita | `LancamentoService` | Backend implementado; teste específico/aceitação pendente. |
| RF33 | Registrar despesa | `LancamentoService` | Backend com cenário aprovado: liquidacaoParcialAtualizaSaldoERejeitaExcesso. |
| RF34 | Apresentar fluxo de caixa | `FluxoCaixaService` | Backend com cenário aprovado: compraGeraEstoqueObrigacaoMasNaoCaixa. |
| RF35 | Calcular custo por safra | `CustoService` | Backend implementado; teste específico/aceitação pendente. |
| RF36 | Calcular custo por lote de animais | `CustoService` | Backend com cenário aprovado: aplicacaoBaixaUmaVezEPreservaCustoDaOrigem. |
| RF37 | Calcular custo por tarefa | `CustoService` | Backend implementado; teste específico/aceitação pendente. |
| RF38 | Exportar lançamentos para o contador | `RelatorioService (financeiro)` | Parcial: CSV implementado; layout contábil pendente com o contador. |
| RF39 | Atribuir tarefa a funcionário | `TarefaService` | Backend com cenário aprovado: tarefaPreservaTaxaDuracaoENaoRecalculaAposReenvio. |
| RF40 | Acompanhar execução da tarefa | `TarefaService` | Backend com cenário aprovado: tarefaPreservaTaxaDuracaoENaoRecalculaAposReenvio. |
| RF41 | Cadastrar máquina | `MaquinaService` | Backend com cenário aprovado: maquinaMantemUnidadeEImpedeLeituraRegressiva. |
| RF42 | Registrar abastecimento | `AbastecimentoService` | Backend implementado; teste específico/aceitação pendente. |
| RF43 | Registrar horas de uso | `UsoMaquinaService` | Backend com cenário aprovado: maquinaMantemUnidadeEImpedeLeituraRegressiva. |
| RF44 | Registrar manutenção preventiva | `ManutencaoService` | Backend implementado; teste específico/aceitação pendente. |
| RF45 | Registrar manutenção corretiva | `ManutencaoService` | Backend implementado; teste específico/aceitação pendente. |
| RF46 | Exibir painel de indicadores | `DashboardService` | Backend implementado; teste específico/aceitação pendente. |
| RF47 | Gerar relatório gerencial | `RelatorioService` | Backend implementado; teste específico/aceitação pendente. |
| RF48 | Exportar relatório em PDF | `RelatorioService` | Backend com cenário aprovado: relatoriosUsamMesmosDadosParaJsonCsvEPdf. |
| RF49 | Exportar relatório em CSV | `ExportacaoService` | Backend com cenário aprovado: relatoriosUsamMesmosDadosParaJsonCsvEPdf. |
| RF50 | Alertar vencimento de vacinas | `AlertaService` | Backend implementado; teste específico/aceitação pendente. |
| RF51 | Alertar vencimento de insumos | `AlertaService` | Backend implementado; teste específico/aceitação pendente. |
| RF52 | Alertar estoque mínimo | `AlertaService` | Backend implementado; teste específico/aceitação pendente. |
| RF53 | Alertar manutenção de máquinas | `AlertaService` | Backend com cenário aprovado: maquinaMantemUnidadeEImpedeLeituraRegressiva. |
| RF54 | Enviar alertas por e-mail | `AlertaService.enviar` | Parcial: integração SMTP e tratamento implementados; provedor externo não testado. |
| RF55 | Restringir dados por perfil | `ContextoUsuario / DTOs` | Backend com cenário aprovado: restricaoFinanceiroOperadorEOutraFazenda. |
| RF56 | Configurar antecedência dos alertas | `ConfiguracaoAlertaService` | Backend com cenário aprovado: maquinaMantemUnidadeEImpedeLeituraRegressiva. |
| RF57 | Registrar liquidação financeira | `LiquidacaoService` | Backend com cenário aprovado: liquidacaoParcialAtualizaSaldoERejeitaExcesso. |
| RF58 | Consultar trilha de auditoria | `AuditoriaService.consultar` | Backend implementado; teste específico/aceitação pendente. |
| RF59 | Excluir cadastro sem histórico | `Services dos cadastros / integridade JPA` | Backend implementado; teste específico/aceitação pendente. |
| RF60 | Definir calendário sanitário | `CalendarioSanitarioService` | Backend com cenário aprovado: agendamentoNaoBaixaEstoqueEAplicacaoSanitariaConcluiComCarencia. |
| RF61 | Agendar ação sanitária | `AgendamentoSanitarioService` | Backend com cenário aprovado: agendamentoNaoBaixaEstoqueEAplicacaoSanitariaConcluiComCarencia. |
| RF62 | Planejar manutenção preventiva | `PlanoManutencaoService` | Backend com cenário aprovado: maquinaMantemUnidadeEImpedeLeituraRegressiva. |
| RF63 | Movimentar lote entre pastagens | `MovimentacaoPastagemService` | Backend implementado; teste específico/aceitação pendente. |

## Regras de domínio

| RD | Implementação e limite |
|---|---|
| RD01 | Vínculos de fazenda/atividade/responsável e auditoria por operação; conferir todos os históricos na aceitação. |
| RD02 | Aviso de carência/pendência em produção e receita de venda; parâmetros técnicos não são inventados. |
| RD03 | Uso vencido ou com validade pendente é bloqueado; descarte separado. Cenário testado. |
| RD04 | Alertas consultam validade de partidas e antecedência; configuração ausente é indicada. |
| RD05 | Calendário por espécie/lote, referência técnica e agendamento; aplicação conclui agenda. Cenário testado. |
| RD06 | Identificação individual única por fazenda; índice e validação de serviço. |
| RD07 | Talhão com fazenda e limites textuais; versão anterior preservada. |
| RD08 | Um vínculo aberto por animal, histórico e transferência. Cenário testado. |
| RD09 | Lote vinculado à pastagem; mudança preserva origem/destino/data. Validar movimento em cenários adicionais. |
| RD10 | Consumo por preço de origem e mão de obra histórica; não duplica compra/pagamento. Conferir todas as categorias e origens financeiras na aceitação. |
| RD11 | HORAS/KM separados, leituras não regressivas, planos e alertas por unidade. Cenário testado. |
| RD12 | Produtividade agrupada por origem/produto/unidade/área/lote histórico. Testes comparativos adicionais pendentes. |
| RD13 | Histórico sanitário consultável/exportável, com filtros e referências. Validação com consultor pendente. |
| RD14 | Histórico produtivo consultável/exportável; validação adicional de filtros pendente. |
| RD15 | Histórico de aplicações consultável/exportável; validação com consultor pendente. |

## Requisitos não funcionais

| Requisito | Situação |
|---|---|
| RNF01 | Paginação até 500; desempenho de ponta a ponta em dois segundos não medido. |
| RNF05–RNF06 | Telas preservadas; matriz de dispositivos e SUS não executados. |
| RNF07–RNF08 | Backup/restauração de operação dependem de procedimento, ambiente e responsável; não foram executados. |
| RNF09/RNF13 | Controles de acesso e minimização; política e prazos LGPD não aprovados. |
| RNF10–RNF11 | Sessão, perfil, CSRF e conta ativa; alguns cenários HTTP aprovados, matriz completa pendente. |
| RNF12 | Sem publicação HTTPS nesta entrega. |
| RNF14 | Nenhuma auditoria de vulnerabilidades concluída; não se declara ausência de críticas. |
| RNF15 | Código Java, compilado/verificado com JDK 21. |
| RNF16 | Migrations, validação JPA e concorrência executadas em PostgreSQL 18.1. |
| RNF17 | Há testes automatizados, mas ainda não há inventário completo que permita aferir 70% das regras. |
