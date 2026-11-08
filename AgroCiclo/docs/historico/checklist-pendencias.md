# Checklist técnico pós-revisão — 07/11/2026

Legenda: **Parcial** (API limitada), **Pendente** (não implementado), **A validar** (sem execução comprovada).

| Módulo do plano | Situação nesta entrega | Próxima ação |
|---|---|---|
| Núcleo/perfis/setup | Parcial | propriedade múltipla, exclusão segura, controles LGPD, testes autorização |
| Cadastros de cargo/funcionário/talhão | Parcial | edição e histórico dos cadastros |
| Lotes/pastagens/animais/transferência | Parcial | movimentação de pastagem, histórico sanitário |
| Pesagens | Parcial | consultar evolução gráfica e integração com relatórios |
| Agricultura (safras) | Parcial | plantio, manejo, colheita, encerramento |
| Estoque/partidas/movimentos | Parcial | compras, fornecedores, recebimento, aplicações, alertas mínimos |
| Financeiro | Pendente | lançamentos, liquidações, caixa, custos |
| Tarefas | Pendente | atribuição, execução, custo por tarefa |
| Maquinário | Pendente | horímetro, abastecimento, manutenção |
| Relatórios/alertas | Pendente | consolidação de métricas, CSV/PDF, e-mail |
| Integração de telas | Parcial | integrar telas originais, listas e pesquisa |
| Segurança, Postgres, testes de integração e carga | A validar | Maven, Docker/Postgres, testes com login e papéis |
| Pendências D01–D12 | A validar | decisão formal pela equipe |

**Rastreabilidade:** a matriz detalhada original `matriz-requisitos.md` não foi alterada para marcar requisitos inteiros como concluídos por conta de um único endpoint. Sua reavaliação depende de testes de aceitação por RF/RD.
