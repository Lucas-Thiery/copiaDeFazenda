# Catálogo de APIs

Base: `http://localhost:8080`. Todas as rotas abaixo exigem sessão, salvo login e setup. Os exemplos completos dos DTOs estão no código e os cenários executáveis em `src/test/java`.

## Convenções

- `GET` de coleção aceita `pagina` e `tamanho` (máximo 500).
- `GET /recurso/{id}` utiliza ID próprio `Long`.
- Gravações autenticadas exigem `Idempotency-Key` e token CSRF; mantenha cookies de sessão.
- `X-Fazenda-Id` é opcional e só admite fazendas vinculadas ao usuário.
- Campos `cod...` informam IDs relacionados, sem objetos JPA no JSON.
- P = Proprietário; G = Gerente de Campo; O = Operador. Algumas consultas omitem valores financeiros conforme o perfil.

## Recursos por entidade

| Recurso | Base canônica | Operações | Perfil de gravação | Perfil de consulta |
|---|---|---|---|---|
| Fazenda | `/api/fazendas` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | P | PGO |
| Cargo | `/api/cargos` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | P | P |
| Funcionario | `/api/funcionarios` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PG |
| Usuario | `/api/usuarios` | GET coleção; GET /{id}; POST | P | P |
| Talhao | `/api/talhoes` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PG |
| Pastagem | `/api/pastagens` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PG |
| Lote | `/api/lotes` | GET coleção; GET /{id}; POST | PG | PGO |
| Animal | `/api/animais` | GET coleção; GET /{id}; POST | PG | PGO |
| VinculoAnimalLote | `/api/vinculos-animal-lote` | GET coleção; GET /{id} | — | PGO |
| MovimentacaoPastagem | `/api/movimentacoes-pastagem` | GET coleção; GET /{id}; POST | PG | PG |
| Pesagem | `/api/pesagens` | GET coleção; GET /{id}; POST | PGO | PGO |
| Safra | `/api/safras` | GET coleção; GET /{id}; POST | PG | PG |
| PlanoManejo | `/api/planos-manejo` | GET coleção; GET /{id}; POST | PG | PG |
| EtapaCultivo | `/api/etapas-cultivo` | GET coleção; GET /{id}; POST | PG | PG |
| ProducaoColheita | `/api/producoes-colheita` | GET coleção; GET /{id}; POST | PG | PG |
| Manejo | `/api/manejos` | GET coleção; GET /{id}; POST | PG | PG |
| ItemEstoque | `/api/estoque/itens` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PGO |
| Fornecedor | `/api/fornecedores` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PG |
| Compra | `/api/compras` | GET coleção; GET /{id}; POST | PG | PG |
| ItemCompra | `/api/itens-compra` | GET coleção; GET /{id} | — | PG |
| PartidaEstoque | `/api/estoque/partidas` | GET coleção; GET /{id}; POST | PG | PGO |
| MovimentacaoEstoque | `/api/estoque/movimentacoes` | GET coleção; GET /{id}; POST | PG | PGO |
| Aplicacao | `/api/aplicacoes` | GET coleção; GET /{id}; POST | PGO | PGO |
| CalendarioSanitario | `/api/calendarios-sanitarios` | GET coleção; GET /{id}; POST | PG | PG |
| AgendamentoSanitario | `/api/agendamentos-sanitarios` | GET coleção; GET /{id}; POST | PG | PG |
| ProducaoPecuaria | `/api/producoes-pecuarias` | GET coleção; GET /{id}; POST | PGO | PGO |
| Maquina | `/api/maquinas` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PG |
| PlanoManutencao | `/api/planos-manutencao` | GET coleção; GET /{id}; POST | PG | PG |
| Manutencao | `/api/manutencoes` | GET coleção; GET /{id}; POST | PG | PG |
| Abastecimento | `/api/abastecimentos` | GET coleção; GET /{id}; POST | PGO | PGO |
| UsoMaquina | `/api/usos-maquina` | GET coleção; GET /{id}; POST | PGO | PGO |
| Tarefa | `/api/tarefas` | GET coleção; GET /{id}; POST | PG | PGO |
| Lancamento | `/api/lancamentos` | GET coleção; GET /{id}; POST | P | P |
| Liquidacao | `/api/liquidacoes` | GET coleção; GET /{id}; POST | P | P |
| ConfiguracaoAlerta | `/api/configuracoes-alerta` | GET coleção; GET /{id}; POST; PUT /atualizar/{id}; DELETE /deletar/{id} | PG | PG |
| VersaoLimites | `/api/versoes-limites` | GET coleção; GET /{id} | — | PG |
| VersaoPlanejamento | `/api/versoes-planejamento` | GET coleção; GET /{id} | — | PG |

Também há aliases sem `/api` para os recursos das entidades, preservando o estilo dos exemplos. Fazenda aceita `/api/propriedades` como alias. Eventos e históricos não podem ser apagados por CRUD.

## Operações específicas

| Método e rota | Finalidade | Perfil |
|---|---|---|
| `POST /api/cadastro/proprietario` | Cria conta por e-mail com nome, senha e aceite dos termos. Exige CSRF | Público |
| `POST /api/cadastro/primeira-fazenda` | Cria e vincula a primeira fazenda. Exige CSRF e conta pendente | P |
| `POST /api/setup/inicial` | Primeira conta; cabeçalho X-Setup-Token | Implantação; apenas uma vez |
| `POST /api/auth/login` | Formulário login/senha; inicia sessão | P/G/O |
| `POST /api/auth/logout` | Encerra sessão; exige CSRF | P/G/O |
| `GET /api/csrf` | Token e nome do cabeçalho CSRF | Público |
| `GET /api/sessao` | Conta, perfil, fazendas e pendência da primeira fazenda | P/G/O |
| `POST /api/animais/{id}/transferencias` | Fecha vínculo e cria novo | P/G |
| `GET /api/pesagens/evolucao` | codAnimal ou codLote, dtaInicio, dtaFim | P/G/O |
| `POST /api/tarefas/{id}/concluir` | Início/fim reais e custo histórico | P/O; Operador somente a própria |
| `PATCH /api/usuarios/{id}/desativacao` | Desativa sem apagar autoria | P |
| `PUT /api/usuarios/{id}/email` | Atualiza endereço para alertas | P |
| `PUT /api/planos-manejo/atualizar/{id}` | Revisão com versão anterior preservada | P/G |
| `GET /api/auditoria` | dtaInicio, dtaFim, operacao opcional | P |
| `GET /api/custos/{destino}/{id}` | Destino SAFRA/LOTE/TAREFA, período | P |
| `GET /api/fluxo-caixa` | Liquidações e pendências; período | P |
| `GET /api/dashboard` | Custos, produtividade e fluxo; período | P |
| `GET /api/relatorios/{tipo}` | json/csv/pdf, período e filtros | P |
| `GET /api/alertas` | dtaReferencia e categoria opcionais | P/G |
| `POST /api/alertas/enviar` | Envio ao endereço do usuário atual | P/G |

Tipos de relatório: `custos`, `produtividade`, `produtivo`, `sanitario`, `aplicacoes`, `financeiro`. Filtros opcionais: `codSafra`, `codLote`, `codAnimal`; o serviço valida a aplicabilidade. O layout contábil é provisório.

## Exemplos JSON

Exemplos usam IDs ilustrativos; utilize os IDs retornados no seu banco. A fazenda de cada operação é determinada pela sessão/cabeçalho e nunca aceita de forma irrestrita um ID informado no corpo.

### Animal — POST /api/animais

```json
{
  "identificacao": "BR-001",
  "especie": "Bovino",
  "raca": "Nelore",
  "peso": 180.5,
  "idadeMeses": 12,
  "dtaNascimento": "2025-10-01",
  "dtaEntrada": "2026-11-07",
  "codLoteAtual": 1
}
```

### Conta — POST /api/usuarios

```json
{
  "codFuncionario": 2,
  "login": "operador",
  "senha": "senha-ficticia-123",
  "perfil": "OPERADOR",
  "codFazendas": [
    1
  ],
  "email": "pessoa@example.com"
}
```

### Compra — POST /api/compras

```json
{
  "codFornecedor": 1,
  "referencia": "NF-001",
  "dtaOperacao": "2026-11-07",
  "total": 30,
  "itens": [
    {
      "codItem": 1,
      "quantidade": 10,
      "unidade": "kg",
      "valorUnitario": 3,
      "codigoLote": "P-01",
      "dtaValidade": "2027-01-01"
    }
  ]
}
```

### Entrada sem compra — POST /api/estoque/partidas

```json
{
  "codItem": 1,
  "dtaEntrada": "2026-11-07",
  "dtaValidade": "2027-01-01",
  "quantidadeInicial": 10,
  "valorUnitarioOrigem": 3,
  "referenciaOrigem": "DOACAO-001",
  "unidade": "kg"
}
```

### Aplicação de ração — POST /api/aplicacoes

```json
{
  "codPartida": 1,
  "codLote": 1,
  "codResponsavel": 2,
  "dtaOperacao": "2026-11-07",
  "quantidade": 2,
  "unidade": "kg",
  "referencia": "MANEJO-001"
}
```

### Transferência — POST /api/animais/1/transferencias

```json
{
  "codLoteDestino": 2,
  "dtaOperacao": "2026-11-07"
}
```

### Liquidação — POST /api/liquidacoes

```json
{
  "codLancamento": 1,
  "dtaOperacao": "2026-11-07",
  "valor": 10,
  "comprovante": "PIX-001"
}
```

### Conclusão de tarefa — POST /api/tarefas/1/concluir

```json
{
  "dtaInicio": "2026-11-07T08:00:00",
  "dtaFim": "2026-11-07T10:00:00",
  "observacoes": "Atividade executada"
}
```

### Novo ciclo e primeiro preparo — POST /api/etapas-cultivo

```json
{
  "etapa": "PREPARO",
  "dtaOperacao": "2026-11-07",
  "novaSafra": {
    "codTalhao": 1,
    "identificacao": "MILHO-2026",
    "cultura": "Milho",
    "areaPlantadaHa": 10,
    "dtaInicio": "2026-11-07"
  }
}
```

### Configuração — POST /api/configuracoes-alerta

```json
{
  "diasVacina": 5,
  "diasValidade": 7,
  "diasManutencao": 3,
  "margemHoras": 10,
  "margemKm": 20
}
```

## Consultar resultado de reenvio

Reenvie a mesma rota com a mesma Idempotency-Key e os mesmos dados. O servidor retorna o resultado existente sem repetir os efeitos. Chaves de outro usuário/fazenda não concedem acesso. Se o conteúdo ou o perfil mudou, a chave é recusada. Na consulta de uma operação física use também a referência de negócio, por exemplo referência de compra + fornecedor.

Em erro de rede, preserve a chave. Para corrigir dados de uma solicitação recusada ou criar um novo acontecimento, utilize outra chave. O envio SMTP não tem garantia de entrega exatamente uma vez: consulte o provedor diante de resultado desconhecido.
