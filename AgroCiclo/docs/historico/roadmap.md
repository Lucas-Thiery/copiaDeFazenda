# Roadmap de implementação

O escopo segue os incrementos e requisitos funcionais do Plano de Projeto/Especificação de Requisitos. Uma tela HTML preservada não é evidência de requisito implementado: a conclusão exige persistência, validação, autorização e teste do fluxo correspondente.

## Incremento 1 — núcleo, segurança e cadastros básicos

| Requisito | Situação nesta revisão |
|---|---|
| RF01–RF03 | Implementação inicial: login por sessão, logout Spring Security e autorização por perfil |
| RF04 | Configuração inicial de propriedade implementada; cadastro posterior multi-propriedade depende de decisão de associação |
| RF05–RF07 | Talhão, cargo e funcionário implementados |
| RF08–RF10 | Criação de conta, perfil fixo e desativação implementados |
| RF11/RF58 | Auditoria básica de inclusão, transferência e desativação; consulta pelo Proprietário |
| RF12–RF14 | Animal, lote e pastagem implementados |
| RF15 | Transferência de animal implementada antecipadamente, com histórico temporal e bloqueio de concorrência |
| RF55 | Consultas iniciais filtradas pela propriedade do usuário; matriz completa e testes negativos ainda pendentes |
| RF59 | Exclusão segura de cadastros com histórico ainda não exposta; exclusão física não é disponibilizada pela API |

## Incremento 2 — pecuária, agricultura, estoque e compras

**Ainda não implementado de ponta a ponta.** Há um rascunho de modelo SQL em `docs/sql/`, mas ele não foi promovido ao Flyway nem validado em PostgreSQL. Deve contemplar RF16–RF31 e RF60–RF61/RF63, com transações atômicas, validade de partidas, bloqueio de saldo negativo, histórico de movimentações, calendário sanitário com referência técnica e vínculo de custo à origem real.

## Incremento 3 — financeiro, pessoas, máquinas, relatórios e alertas

**Ainda não implementado de ponta a ponta.** O rascunho de modelo SQL contempla estruturas iniciais para os domínios, mas não há serviços, APIs, telas integradas ou testes desses fluxos. Deve contemplar RF32–RF54, RF56–RF57 e RF62. Fluxo de caixa deve considerar liquidações efetivas; compras, pagamentos e consumo não podem duplicar despesas; manutenção por uso deve comparar unidades compatíveis.

## Critérios mínimos de conclusão de cada requisito

1. Entidade e migração Flyway versionada quando houver persistência.
2. Regra de negócio em serviço transacional.
3. Validação de entrada e erro compreensível.
4. Autorização e isolamento por propriedade no servidor.
5. Testes de sucesso, dados inválidos, perfil não autorizado e conflito/duplicidade aplicáveis.
6. Tela conectada à API, sem `alert()` de sucesso fictício.
7. Atualização da matriz de rastreabilidade e documentação.

## Rastreabilidade e decisões

- Matriz RF01–RF63 e RD01–RD15: [`matriz-requisitos.md`](matriz-requisitos.md).
- Decisões D01–D12: [`decisoes-pendentes.md`](decisoes-pendentes.md).
- Estado detalhado por incremento: [`implementacao-plano.md`](implementacao-plano.md).
