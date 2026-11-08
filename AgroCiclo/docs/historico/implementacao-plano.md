# Implementação vinculada ao plano AgroCiclo

Este documento é a referência de rastreabilidade entre o plano, os requisitos funcionais (RF), as regras de domínio (RD) e o estado do repositório. A presença de uma tabela SQL não significa que o requisito esteja implementado de ponta a ponta: para ser considerado concluído, também precisa de serviço, autorização, API, interface, testes e evidência de execução.

## Critério de conclusão

Um RF só pode receber estado **Concluído** quando houver: (1) modelo persistente/migration; (2) regra de negócio; (3) validação de entrada; (4) autorização por perfil e escopo de propriedade; (5) endpoint; (6) interface integrada quando aplicável; (7) testes automatizados; e (8) evidência de execução. O estado **Fundação de dados** indica apenas que existe parte do modelo relacional, não uma funcionalidade utilizável.

## Incremento 1 — núcleo, segurança e cadastros essenciais

| Requisito | Escopo do plano | Estado neste repositório |
|---|---|---|
| RF01–RF03 | Autenticação, sessão e autorização por perfil | Implementação inicial no backend; método definitivo depende de D05 |
| RF04 | Cadastro de propriedade | Implementação inicial |
| RF05 | Cadastro de talhão | Implementação inicial |
| RF06 | Cadastro de cargo | Implementação inicial |
| RF07 | Cadastro de funcionário e taxa efetiva | Implementação inicial |
| RF08–RF10 | Usuário, perfil e desativação | Implementação inicial |
| RF11 | Auditoria de operações | Implementação inicial; cobertura integral de alterações/exclusões precisa ser verificada |
| RF12–RF14 | Animal, lote e pastagem | Implementação inicial |
| RF55 | Restrição de dados por perfil/propriedade | Implementação inicial; revisar em cada novo endpoint e exportação |
| RF58 | Consulta da trilha de auditoria | Endpoint inicial existente; filtros e testes de autorização precisam de verificação |
| RF59 | Exclusão apenas sem histórico e sem dependências | Pendente |

## Incremento 2 — manejo pecuário, agricultura, estoque e compras

O rascunho SQL `docs/sql/rascunho-modelo-operacional-incrementos-2-e-3.sql` descreve uma primeira fundação relacional para os itens abaixo. **Nenhum dos RF deste quadro deve ser considerado concluído apenas por essa migration.** Ainda são necessários serviços transacionais, endpoints, interface e testes.

| Requisito | Escopo | Fundação de dados criada |
|---|---|---|
| RF15 | Transferir animal entre lotes | Histórico de vínculo já existe no Incremento 1; revisar fluxo completo e autorização |
| RF16–RF17 | Pesagens e evolução de peso | `pesagens_animais` |
| RF18–RF19 | Vacinação e tratamento sanitário | `eventos_sanitarios` |
| RF20 | Produção animal | `producoes_animais` |
| RF21 | Planejar safra | `safras` |
| RF22 | Registrar plantio | `plantios` |
| RF23 | Manejo da lavoura | `manejos_lavoura` |
| RF24–RF25 | Aplicação e baixa atômica de insumos | `aplicacoes_insumos`, `movimentacoes_estoque` e trigger de saldo; atomicidade do caso de uso ainda precisa de serviço transacional e testes |
| RF26 | Colheita | `colheitas` |
| RF27 | Itens de estoque | `itens_estoque` |
| RF28 | Fornecedores | `fornecedores` |
| RF29 | Compra ligada a estoque e obrigação | `compras`, `itens_compra` e vínculo opcional único com lançamento; falta implementar recebimento idempotente |
| RF30 | Entrada por partida | `partidas_estoque`, `recebimentos_compra` e `movimentacoes_estoque` |
| RF31 | Saída, uso e descarte | `movimentacoes_estoque` e trigger que impede saldo negativo e uso de partida vencida |
| RF60–RF61 | Calendário e agendamento sanitário | `calendarios_sanitarios`, `itens_calendario_sanitario`, `agendamentos_sanitarios` |
| RF63 | Transferência de lote entre pastagens | `movimentacoes_lote_pastagem` |

## Incremento 3 — financeiro, pessoas, máquinas, relatórios e alertas

| Requisito | Escopo | Fundação de dados criada |
|---|---|---|
| RF32–RF33 | Receitas e despesas | `lancamentos_financeiros` |
| RF34 | Fluxo de caixa | `liquidacoes_financeiras`; consulta agregada ainda pendente |
| RF35–RF37 | Custos por safra, lote e tarefa | `custos_operacionais`; cálculo e prevenção de dupla contagem ainda pendentes |
| RF38 | Exportação financeira CSV | Pendente |
| RF39–RF40 | Atribuição e execução de tarefas | `tarefas`, `atribuicoes_tarefa`, `execucoes_tarefa` |
| RF41–RF45 | Máquinas, abastecimento, horímetro e manutenção | `maquinas`, `abastecimentos`, `leituras_medidor`, `planos_manutencao`, `manutencoes` |
| RF46–RF49 | Painel, relatórios, PDF e CSV | Pendente |
| RF50–RF53 | Alertas sanitários, validade, estoque mínimo e manutenção | `configuracoes_alerta`, `alertas`; regras geradoras ainda pendentes |
| RF54 | Envio de alertas por e-mail | `tentativas_email_alerta`; integração de e-mail pendente |
| RF56 | Antecedência dos alertas | `configuracoes_alerta` |
| RF57 | Liquidações parciais | `liquidacoes_financeiras` e trigger que impede exceder o valor previsto |
| RF62 | Planejamento de manutenção preventiva | `planos_manutencao` |

## Regras de domínio que precisam permanecer explícitas

- **RD01:** rastreabilidade por propriedade, atividade, data, responsável e origem.
- **RD02 e RD05:** carência e calendário sanitário dependem de orientação técnica identificada. Sem parâmetros validados, o sistema deve sinalizar pendência, não liberar consumo/venda nem recomendar doses.
- **RD03 e RD04:** saída para uso de partida vencida é bloqueada; validade é avaliada por partida e antecedência configurada.
- **RD06–RD09:** identificação animal única por propriedade; animal em um lote por vez; lote associado a pastagem; talhão é distinto de pastagem.
- **RD10:** custo usa consumo real valorado pela origem, mão de obra com taxa preservada e custos diretamente vinculados; compra, pagamento e componentes já contabilizados não podem ser somados novamente. Não há rateio automático de despesas gerais.
- **RD11:** manutenção por uso compara valores na mesma unidade; não converte horímetro em dias.
- **RD12:** produtividade só pode ser comparada com produto, unidade, período e base compatíveis.
- **RD13–RD15:** exportações históricas para consultores devem preservar período, filtros e identificação dos registros.

## Estado das verificações

- O XML do `pom.xml` e a integridade estrutural do ZIP foram verificados na versão anterior.
- A execução do Maven Wrapper foi tentada nesta revisão, mas o download do Maven 3.9.11 falhou ao acessar Maven Central. Portanto, compilação e testes não foram executados.
- O rascunho SQL V2 não foi colocado no diretório ativo do Flyway: precisa ser revisado, aplicado em PostgreSQL real e testado em banco limpo e em atualização a partir de V1 antes de virar migration executável.
- Não há evidência de teste de desempenho, restauração, compatibilidade móvel ou segurança de produção.

## Próximo trabalho de implementação

1. Validar e aplicar V2 em PostgreSQL; corrigir qualquer erro de DDL observado.
2. Implementar serviços transacionais de estoque/compra/aplicação, incluindo idempotência e atualização de saldos.
3. Criar entidades JPA, DTOs, repositórios, controllers e testes de RF16–RF31 e RF60–RF63.
4. Conectar as telas correspondentes e validar fluxos por perfil.
5. Implementar financeiro, tarefas e máquinas com snapshots de custos e liquidação parcial.
6. Implementar cálculos, relatórios, exportações e alertas sem valores fictícios.
7. Executar a matriz de testes e atualizar este documento com evidências reais.
