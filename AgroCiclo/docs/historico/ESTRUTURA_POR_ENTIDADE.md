# Estrutura por entidade — Java 21 / Spring Boot 3.5.7

Esta revisão utiliza **pastas por camada** (não módulos com cinco subpastas em cada um):

```text
src/main/java/com/ufs/gestaofazenda/
├── entity/       # Animal.java, Lote.java, Safra.java, etc.
├── dto/          # AnimalRequest.java, SafraResponse.java, etc.
├── repository/   # AnimalRepository.java, LoteRepository.java, etc.
├── service/      # AnimalService.java, LoteService.java, etc.
├── controller/   # AnimalController.java, LoteController.java, etc.
├── common/       # configuração, exceções e auditoria transversal
└── GestaofazendaApplication.java
```

- Regras próprias de animais estão no `AnimalService`; cadastros não dependem mais do `CadastrosService` genérico.
- Estoque foi separado em `ItemEstoqueService`, `PartidaEstoqueService` e `MovimentacaoEstoqueService`. `EstoqueController` conserva as rotas existentes e delega para o serviço adequado.
- Para cada entidade JPA existente foi preservado seu `Repository`. `Propriedade` e `VinculoAnimalLote` agora também têm serviços próprios de consulta.
- O bootstrap inicial continua no `SetupService`, por ser um caso de uso transacional que envolve **várias entidades**.
- Auditoria permanece em `common/audit/` como funcionalidade transversal.
- `Perfil` é enum, portanto não necessita repository ou service.

## Limites

O repositório mantém o **rascunho** do SQL para funcionalidades futuras em `docs/sql/`, sem ativar migrations novas para tabelas ainda não implementadas. Não se deve confundir requisitos previstos com funcionalidades concluídas. Os módulos de financeiro, máquinas, tarefas, compras e sanidade exigem implementação específica conforme o plano.

## Verificação

A estrutura e referências Java foram revisadas, mas **não houve compilação Maven nem execução de testes em ambiente com dependências Spring Boot disponíveis** nesta execução. Execute `./mvnw test` (Windows: `mvnw.cmd test`) e depois valide com PostgreSQL antes de integrar na branch principal.
