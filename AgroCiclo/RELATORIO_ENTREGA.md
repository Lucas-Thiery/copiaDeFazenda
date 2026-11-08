# Relatório de implementação — AgroCiclo

Data: 07/11/2026. Referência: `AgroCiclo_Parte1_atualizado.pdf`, `Document1.pdf`, texto colado e cinco exemplos de Animal. Foram comparados os ZIPs `agrociclo-plano-completo-em-desenvolvimento.zip` e `AgroCiclo_Java21_estrutura_por_entidade.zip`. A versão por entidade foi utilizada como base local; configurações, recursos estáticos, wrapper, CI e migrations anteriores foram preservados.

## Resultado e limites da entrega

A entrega contém código executável de backend para os três incrementos, seguindo a organização e o padrão de DTOs solicitados. Há cadastros, histórico pecuário, agricultura, estoque, compras, financeiro, tarefas, máquinas, custos, relatórios e alertas. **A presença dessas APIs não representa a conclusão integral dos 63 RF e de todas as telas e exceções dos casos de uso.** A matriz distingue implementação, evidência disponível e validações pendentes.

Não é uma entrega homologada de produção. Continuam pendentes integração completa das telas, revisão humana das regras e permissões em todos os cenários, avaliação de desempenho, SUS/UAT, inventário de cobertura de regras, análise de vulnerabilidades e decisões operacionais da equipe. O envio SMTP não foi efetuado contra um provedor real.

## Padrão aplicado

| Item | Implementação |
|---|---|
| Linguagem e framework | Java 21, Spring Boot 3.5.7, Maven Wrapper |
| Persistência | PostgreSQL, JPA, Flyway e migrations incrementais |
| Identificador próprio | `Long animalId`, `Long usuarioId`, `Long fazendaId` e equivalentes |
| Identificador relacionado | `Long codFazenda`, `Long codLoteAtual`, `Long codFuncionario`; N–N como `Set<Long> codFazendas` |
| Entidades | `@Data`, JPA, relacionamentos Java com `@JoinColumn(name = "cod_...")` |
| DTOs | Classe externa por entidade, classes internas `EntidadeRequest` / `EntidadeResponse` |
| Services | `@Service`, `@RequiredArgsConstructor`, repositório próprio, mapeamento explícito e métodos privados de aplicação/conversão/busca |
| Controllers | `ResponseEntity`, validação de entrada e delegação ao serviço |
| Tipos | `BigDecimal` para valores e medidas; `LocalDate`, `LocalDateTime` e `Instant` conforme o significado; datas com `dta` |
| Erros | `ResponseStatusException` e respostas simples de validação/integridade |

A orientação da mensagem mais recente prevaleceu sobre os exemplos antigos: o `Integer idAnimal` dos anexos foi substituído por `Long animalId`. Datas e números não utilizam `@NotBlank`. DELETE não recebe corpo de cadastro.

## O que foi preservado e corrigido

O projeto base já continha autenticação por sessão, BCrypt, CSRF, criação inicial protegida por token, cadastros do primeiro incremento, vínculo animal–lote, transferência, pesagem individual, planejamento básico de safra e entrada/saída de estoque. Esses comportamentos foram mantidos e adaptados aos novos DTOs e nomes.

A base reorganizada ainda utilizava DTOs separados em records, retornava entidades em alguns controllers e tinha imports incorretos em transferência. Parte dos vínculos de estoque e safra era apenas `Long`, em vez de referência JPA. As classes foram reescritas nas camadas solicitadas para corrigir esses pontos, preservando tabelas e regras válidas. O rascunho SQL dos módulos futuros continua em `docs/sql/`, como referência histórica, e não é executado como migration.

## Entidades e camadas

Foram organizados 40 arquivos em Entity (incluindo Perfil e registros técnicos), 46 DTOs, 39 repositories, 47 services e 45 controllers. A relação completa está em `docs/INVENTARIO.md`.

| Área | Entidades principais |
|---|---|
| Núcleo | Fazenda, Cargo, Funcionario, Usuario, Perfil, Auditoria, Solicitacao |
| Pecuária | Pastagem, Lote, Animal, VinculoAnimalLote, MovimentacaoPastagem, Pesagem, CalendarioSanitario, AgendamentoSanitario, ProducaoPecuaria |
| Agricultura | Talhao, VersaoLimites, Safra, PlanoManejo, VersaoPlanejamento, EtapaCultivo, ProducaoColheita, Manejo |
| Estoque/compras | ItemEstoque, Fornecedor, Compra, ItemCompra, PartidaEstoque, MovimentacaoEstoque, Aplicacao |
| Pessoas | Tarefa, com dados de execução e taxa histórica preservados |
| Financeiro | Lancamento, Liquidacao; receitas e despesas diferenciadas por tipo |
| Máquinas | Maquina, PlanoManutencao, Manutencao, Abastecimento, UsoMaquina |
| Alertas | ConfiguracaoAlerta; alertas são consultas derivadas dos registros |

Operações coordenadas entre entidades utilizam os serviços específicos. Não há um `CadastrosService` genérico. Serviços de consulta, autorização, rebanho, carência e exportação existem para regras compartilhadas e não substituem os serviços das entidades.

## Regras implementadas

**Propriedades e acesso.** Conta vinculada a funcionário e relação N–N com fazendas. Cada consulta e referência é limitada à fazenda selecionada e ao perfil. Proprietário administra usuários e acessa finanças; Gerente opera os módulos permitidos; Operador acessa as próprias tarefas e operações de campo autorizadas. Sessões de contas desativadas ou com perfil alterado são recusadas. A desativação do último Proprietário ativo de qualquer fazenda é bloqueada. Senhas são BCrypt e não integram responses ou auditorias.

**Cadastros.** Validações de nomes/identificações, áreas, taxas e vínculos. A taxa efetiva do funcionário utiliza a taxa individual ou a referência do cargo. Atualização dos limites do talhão e do planejamento preserva versões anteriores. Exclusões de referências são bloqueadas por dependências no banco; operações e auditoria não são apagadas.

**Rebanho.** Cadastro de lote antes dos animais, vínculo inicial com pastagem, identificação individual única, pesagem inicial, transferência com fechamento do vínculo anterior e abertura do novo. Transferências retroativas que atingem operações posteriores são bloqueadas de forma conservadora. A movimentação de pastagem altera o local do lote sem recriar seus membros. Pesagens individuais e coletivas mantêm a composição de referência; a evolução não substitui medições antigas.

**Agricultura.** Planejamento sem consumo ou despesa; novo ciclo e primeiro preparo podem ser criados na mesma solicitação. Sequência preparo–plantio–colheita–encerramento, datas não decrescentes, colheitas parciais com unidade consistente, perda total justificada e encerramento explícito. Há registros de manejo e vínculo opcional das aplicações ao manejo.

**Estoque e compras.** Compra recebida gera itens, partidas, entradas e uma obrigação financeira na mesma transação, sem pagamento automático. Entrada manual exige origem, unidade e preço documentados. Consumo verifica saldo, validade, unidade, data e destino; descarte é distinto de uso. Aplicações agrícolas e sanitárias usam o mesmo controle de consumo, preservando quantidade e preço da partida. A aplicação sanitária pode concluir um agendamento. Agendar não baixa estoque. Os avisos de carência indicam pendência quando faltam dados técnicos; não há prescrição de produto/dose.

**Financeiro e custos.** Receita/despesa é obrigação; somente liquidação movimenta caixa. Liquidação parcial não excede o saldo e preserva comprovante. Custo soma consumos, mão de obra histórica e despesas diretas uma vez por origem, sem somar novamente compras ou pagamentos. Tarefa preserva executante, duração, taxa e custo na conclusão; o Operador não recebe o custo. O custo por animal utiliza composição do lote na data final, e o custo por hectare utiliza área histórica da safra. Dados ou denominadores ausentes são informados.

**Máquinas.** Horímetro e odômetro são diferenciados; leitura não pode regredir. Uso não gera tarifa fictícia. Abastecimento valida máquina abastecível e vincula a despesa de origem. Manutenção registra tipo, serviço, peças descritas, custo e plano opcional; não baixa peças automaticamente. Planos por data/uso não geram despesa até execução. A origem financeira já vinculada a outra operação de máquina não é reutilizada.

**Relatórios/alertas.** Consultas de custos, produtividade, históricos sanitário/produtivo/aplicações e lançamentos contábeis. JSON, CSV e PDF utilizam a mesma consulta. A produtividade agrega somente grupos compatíveis, mantendo origem, produto, unidade, área e lote histórico. O dashboard reutiliza as consultas. Alertas usam antecedentes configurados em dias, horas ou quilômetros; configuração ou validade ausente é indicada. SMTP é opcional e retorna resultado sem apagar alertas.

**Reenvio e concorrência.** Gravações autenticadas requerem Idempotency-Key. O resultado e a operação são persistidos na mesma transação e associados a usuário, fazenda, perfil e conteúdo. Uma chave reutilizada com outros dados é recusada. Há também validação de referências de negócio. Gravações são serializadas por fazenda; alterações de contas/fazendas usam uma trava anterior para manter a ordem de bloqueio. Não há reenvio automático de e-mails com resultado desconhecido.

## Verificação

Execução final: **20 testes, 0 falhas, 0 erros, 0 ignorados**. `clean verify` finalizado com código 0.

As evidências ficam em `docs/evidencias/`, e os cenários estão descritos em `docs/TESTES.md`. Foram utilizados JDK 21 completo, Maven Wrapper, H2 e PostgreSQL 18.1 temporário. O teste PostgreSQL aplica V1/V2, insere dados legados, aplica as novas migrations, inicia a aplicação com `ddl-auto=validate`, autentica por HTTP e testa duas saídas simultâneas. Não foi usado apenas um esquema criado pelo H2 para validar as migrations.

## Pendências reais

1. Integrar e validar todas as telas originais e todos os fluxos alternativos/exceções. As telas de exemplo preservadas não cobrem todos os recursos novos.
2. Testar a matriz completa de perfis, isolamento entre múltiplas fazendas, concorrência de liquidações/conclusões e migração de uma cópia representativa da base real. O teste concorrente desta entrega cobre estoque.
3. Revisar a política de duplicidade de operações físicas, especialmente documentos financeiros compostos e entradas legadas que ainda não têm referência de origem. Não são inventadas referências históricas.
4. Validar, com a equipe, a política conservadora para transferências retroativas e as referências técnicas de sanidade/carência. Registros desconhecidos não são apresentados como liberados.
5. Homologar o layout contábil, o conteúdo e a apresentação dos relatórios. PDF usa fonte padrão WinAnsi; caracteres fora dessa codificação devem receber uma solução de fonte incorporada se fizerem parte dos dados reais.
6. Testar SMTP configurado, aceitação, recusa e interrupção. A transação local não fornece garantia de entrega exatamente uma vez em um provedor externo; resultado desconhecido exige conferência antes de nova tentativa.
7. Definir política LGPD, retenção, backup, restauração, HTTPS, hospedagem e responsáveis; executar os procedimentos no ambiente real.
8. Elaborar inventário completo de regras e aferir RNF17. Não foi declarada cobertura de 70%, tempo de dois segundos, SUS ≥70, ausência de vulnerabilidades críticas ou aprovação de UAT.

O pacote não depende de um banco pessoal já preenchido e não inclui credenciais reais. Os exemplos são apenas dados de desenvolvimento. Os arquivos originais enviados não foram substituídos.
