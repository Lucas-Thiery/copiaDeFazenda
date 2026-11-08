# Revisão implementada em 07/11/2026

## Organização
- Entidades das áreas de negócio movidas de `domain` para `entity`, com referências Java ajustadas.
- DTOs de requisição individuais criados em cada módulo e referências de controllers/services atualizadas; `ApiDtos` antigo removido.
- Controller monolítico de cadastros desmembrado em controllers individuais com as mesmas rotas e permissões; `CadastrosService` permanece como implementação interna compartilhada; serviços específicos por cadastro delegam a ele até a migração final das regras.

## Funcionalidades concretas adicionadas
- **Pesagem** por animal: criar e consultar com histórico, datas válidas, isolamento por propriedade e auditoria.
- **Safras**: cadastrar e listar com validação de talhão da propriedade, área e datas.
- **Estoque**: cadastrar itens, registrar entrada por partida, consultar partidas, registrar uso/descarte com bloqueio pessimista contra concorrência, saldo não negativo, impedimento de uso vencido, custo de origem e trilha de movimentações.
- Flyway `V2__pesagens_safras_e_estoque_operacional.sql` ativada para esses recursos específicos.

## Frontend
- A tela `fazendaweb/operacoes-implementadas.html`, acessível no painel principal, permite operar os novos endpoints por formulários reais. As outras telas de demonstração não foram marcadas como prontas.

## APIs adicionadas
- `GET/POST /api/animais/{animalId}/pesagens`
- `GET/POST /api/safras`
- `GET/POST /api/estoque/itens`
- `GET /api/estoque/itens/{id}/partidas`
- `POST /api/estoque/entradas`
- `POST /api/estoque/partidas/{id}/saidas`
- `GET /api/estoque/partidas/{id}/movimentacoes`

## Não implementado / não validado
- Compras e recebimentos associados, sanidade/calendário, produção animal, movimentação lote-pastagem, plantio/manejo/colheita, aplicações, financeiro e liquidações, tarefas/custos, máquinas, dashboards, relatórios, alertas, exportações e integração das telas restantes.
- Setup e vínculo atual de usuário a uma única propriedade não foram convertidos em gerenciamento multi-propriedade.
- Testes end-to-end, homologação no PostgreSQL, carga, acessibilidade e aceite do cliente seguem pendentes.
- Decisões organizacionais D01–D12 requerem validação pela equipe; decisões provisórias não são aprovações.

**Atenção:** o rascunho SQL amplo continua apenas em `docs/sql`; **não** deve ser aplicado junto com V2 sem reconciliação das tabelas já criadas.
