# Revisão do AgroCiclo — 06/11/2026

## 1. Escopo e evidências

Esta é a primeira linha de base de acompanhamento, feita a partir do ZIP `Gestao-de-fazenda-main.zip`, da versão organizada do repositório e do `main.pdf` (Plano de Projeto/Especificação). Não foi fornecido acesso ao histórico remoto de commits, quadro Kanban ou evidências de execução da equipe. Portanto, datas de marcos e itens descritos no plano são tratados como planejamento, não como entregas comprovadas.

## 2. Estado confirmado do código

### Frontend

- As 50 telas HTML do protótipo foram preservadas; a aplicação organizada contém também a página inicial, totalizando 51 arquivos HTML.
- Há duas cópias idênticas de `style.css`, uma na raiz dos recursos estáticos e outra em `fazendaweb/`.
- Não há arquivos JavaScript próprios.
- As referências locais `href` e `src` verificadas nas páginas HTML não apresentam caminhos quebrados.
- Formulários e botões continuam sendo protótipos: não enviam dados para API, não persistem registros e não autenticam usuários. As ações de cadastro/login navegam diretamente para a tela principal.

### Backend e persistência

- Existe a classe de inicialização Spring Boot.
- Existem pacotes organizacionais para segurança, cadastros, agricultura, pecuária, estoque, financeiro, pessoas, máquinas, relatórios, alertas e componentes comuns.
- Os pacotes de módulos contêm apenas `package-info.java`; não há Controllers, Services, Repositories, DTOs ou entidades de domínio implementados.
- O Maven foi centralizado na raiz, com dependências para Spring MVC, JPA, validação, PostgreSQL, Flyway, Actuator e testes.
- A configuração do PostgreSQL está preparada, mas o banco inicia sem tabelas de domínio. `db/migration` ainda não contém migrações SQL.
- Não há autenticação ou autorização implementada. A dependência de segurança foi deliberadamente deixada de fora enquanto D05 não for decidida.

### Infraestrutura e qualidade

- Foram adicionados Docker Compose para PostgreSQL, `.env.example`, `.gitignore` e workflow inicial do GitHub Actions.
- Há somente um teste de contexto do Spring; não há testes de regras de negócio, API, banco ou interface.
- O ambiente desta revisão tem Java 21, mas não tem Maven nem Docker instalados. Assim, não foi possível executar `mvn verify`, iniciar PostgreSQL ou confirmar o pipeline. O XML do Maven foi verificado estruturalmente, mas a resolução das dependências e a compilação permanecem sem validação.

## 3. Correções realizadas nesta revisão

- Corrigido o import do teste que continha os caracteres literais `\\n` no meio da linha.
- Ativado `@ActiveProfiles("test")` para que o teste de contexto use a configuração H2 de testes, em vez de tentar conectar ao PostgreSQL local.
- Removido o arquivo `LICENSE` MIT que havia sido incluído sem decisão aprovada. A decisão D06 permanece pendente e não deve ser presumida.
- Removido um arquivo duplicado de análise de lacunas na raiz; a versão mantida está em `docs/analise-de-lacunas.md`.
- Reexecutada a verificação estática de links locais: zero referências locais quebradas encontradas.

## 4. Alinhamento ao plano e marcos

| Incremento | Conteúdo previsto no plano | Situação observada |
|---|---|---|
| 1 — M3 (30/10) | Núcleo e segurança; propriedades, talhões, cargos, funcionários, pastagens, lotes e cadastro de animais | Não comprovado no código recebido: somente páginas HTML e estrutura inicial de backend |
| 2 — M4 (13/11) | Manejo do rebanho; agricultura e safras; estoque, fornecedores e compras | Não implementado no backend; telas são protótipos |
| 3 — M5 (20/11) | Financeiro e custos; pessoas, tarefas, máquinas; painéis, relatórios e alertas | Não implementado no backend; telas são protótipos |

O plano lista M2 para 09/11 (arquitetura, classes, diagramas, mapeamento objeto-relacional e casos de uso implementados), M4 para 13/11, M5 para 20/11, M6 para 27/11 e M7 para 29/11 às 12h. Nenhum desses marcos deve ser marcado como concluído sem seus entregáveis e evidências.

## 5. Decisões pendentes registradas no plano

| ID | Tema | Situação nesta revisão |
|---|---|---|
| D01 | Propriedade piloto e perfil produtivo | Sem decisão/evidência fornecida |
| D02 | Provedor de nuvem e HTTPS | Sem decisão/evidência fornecida |
| D03 | Versões e configurações das tecnologias | Java 21 e Spring Boot 4.1.0 aparecem no projeto; ainda falta registrar aprovação formal da matriz completa |
| D04 | Relação entre animais, lotes e pastagens | Modelo relacional ainda não existe; decisão bloqueia o modelo de domínio |
| D05 | Método de autenticação | Não decidido/implementado |
| D06 | Licença do código-fonte | Não decidida; arquivo MIT removido |
| D07 | Retenção, bases legais e anonimização LGPD | Sem política registrada nesta entrega |
| D08 | Offline, IoT e integrações futuras | O documento delimita esses itens como fora do escopo atual/evolução futura; registrar decisão final e pontos de extensão até 20/11 |
| D09 | Operação e manutenção após encerramento | Sem decisão/evidência fornecida |
| D10 | Periodicidade e procedimento de teste de restauração | Pendente antes de M6 |
| D11 | Condições do teste de desempenho | Pendente antes de M6 |
| D12 | Liderança do projeto | Sem indicação/evidência fornecida |

O plano também registra DP03: parâmetros sanitários e de carência precisam de validação técnica. Não se deve criar recomendação veterinária nem liberar aplicação com parâmetros não validados.

## 6. Riscos e regras de domínio prioritários

1. **Segurança e isolamento por propriedade:** todas as consultas, alterações, relatórios e exportações devem verificar o vínculo do usuário com a propriedade e o perfil (RF55 e matriz de atores). Esconder links no frontend não é controle de acesso.
2. **Relacionamento pecuário:** um animal pertence a um lote por vez; movimentações precisam preservar histórico, e lotes devem estar associados a pastagens conforme regras do documento.
3. **Estoque:** bloquear saldo negativo, impedir uso de partidas vencidas, preservar origem/preço e distinguir consumo de descarte.
4. **Compra e financeiro:** registrar entrada de estoque e obrigação financeira sem duplicidade; operações relacionadas precisam ser atômicas.
5. **Custos:** evitar dupla contagem entre compra, liquidação e consumo; apropriar consumo usando o preço da origem.
6. **Sanidade:** exigir parâmetros/orientações tecnicamente validados e manter rastreabilidade de aplicação e carência.
7. **Histórico e exclusão:** não apagar registros que sustentam rastreabilidade; definir desativação/arquivamento e auditoria.

## 7. Próximos passos recomendados

### Imediatos — antes de declarar M2/M3 concluídos

1. Reunir e registrar as decisões D01–D07 e D12, com responsável, justificativa e evidência.
2. Aprovar DER, dicionário de dados e cardinalidades para propriedade, usuário, funcionário, cargo, talhão, pastagem, lote e animal.
3. Fechar a matriz de autorização e o método de autenticação antes de expor dados reais.
4. Criar as primeiras migrações Flyway e implementar o núcleo do Incremento 1, com validação no servidor e isolamento por propriedade.
5. Criar testes de integração para cadastros, permissões, unicidade por propriedade e vínculos obrigatórios.
6. Corrigir o frontend para usar formulários reais e chamadas HTTP à API, sem simular sucesso por navegação direta.
7. Executar `mvn verify` em ambiente com Maven e validar o banco com Docker Compose; guardar logs/relatório como evidência.

### Depois do núcleo

- Incremento 2: operações pecuárias, agricultura, estoque e compras com transações, concorrência e histórico.
- Incremento 3: financeiro, tarefas, máquinas, relatórios e alertas, com rastreabilidade de custos.
- Antes de M6: definir e executar teste de restauração (D10), desempenho (D11), segurança e aceitação (UAT).
- Antes de M7: hospedagem/HTTPS (D02), operação e suporte (D09), instruções, release notes, relatório final e demonstração.

## 8. Complementos recomendados ao plano

O plano já contém requisitos, casos de uso, riscos, cronograma, qualidade e testes. Para reduzir ambiguidades de implementação, recomenda-se anexar ou concluir:

- contrato da API (rotas, métodos, JSON, paginação, filtros e erros);
- DER e dicionário de dados com restrições, índices e histórico;
- matriz de permissões traduzida em testes automatizados por caso de uso;
- estratégia transacional e de concorrência para estoque/compras/custos;
- padrão de auditoria, logs e observabilidade;
- formato e regras das exportações;
- política de reenvio/falha/duplicidade dos alertas por e-mail;
- plano de migração e conjunto de dados fictícios para testes/demonstração;
- critérios objetivos de acessibilidade e usabilidade.

Esses complementos detalham a solução; não autorizam ampliar o escopo da versão atual. Permanecem fora do escopo atual, conforme o documento: NF-e, integrações governamentais, marketplace, modelos preditivos de IA, aplicativos nativos, operação totalmente offline e sensores IoT.

## 9. Conclusão

A organização do repositório melhorou a estrutura, a configuração e a documentação, mas o produto ainda não está funcional. O bloqueio mais importante é transformar o protótipo visual em um núcleo executável com modelo de dados, autenticação/autorização, migrações, APIs e testes. A equipe deve replanejar imediatamente o que ainda cabe nos marcos restantes e registrar as entregas reais no Kanban e nos relatórios de teste.
