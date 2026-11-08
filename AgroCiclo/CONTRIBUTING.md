# Como contribuir

## Antes de começar

1. Leia o `README.md` e a documentação em `docs/`.
2. Confira se a alteração está alinhada aos requisitos e decisões aprovados.
3. Para mudanças grandes, abra uma issue antes de implementar.

## Fluxo sugerido

1. Atualize sua branch principal: `git pull --rebase`.
2. Crie uma branch descritiva: `feat/cadastro-propriedade`, `fix/validacao-animal` ou `docs/atualiza-readme`.
3. Faça alterações pequenas e focadas.
4. Execute `./mvnw --batch-mode verify`.
5. Revise o diff para remover arquivos temporários, credenciais e dados pessoais.
6. Abra um Pull Request explicando contexto, solução, testes e limitações.

## Convenção de commits

Use mensagens curtas e no imperativo, preferencialmente com prefixo:

- `feat:` nova funcionalidade
- `fix:` correção
- `docs:` documentação
- `test:` testes
- `refactor:` reorganização sem mudança funcional
- `build:` dependências e build
- `ci:` integração contínua

Exemplo: `feat: adiciona cadastro de propriedades`.

## Requisitos de qualidade

- Não misture refatoração ampla com implementação de funcionalidade sem necessidade.
- Inclua testes para regras de negócio e casos de borda.
- Valide dados no backend.
- Não exponha dados de uma propriedade a usuários de outra propriedade.
- Não inclua segredos, dados reais de fazendas ou informações pessoais.
- Atualize a documentação quando mudar comportamento, configuração ou arquitetura.
- Não declare uma funcionalidade concluída se ela estiver apenas representada por uma tela estática.
