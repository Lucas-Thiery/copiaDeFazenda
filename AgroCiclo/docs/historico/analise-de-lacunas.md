# Análise de lacunas — AgroCiclo

Esta análise compara o repositório recebido com o Plano de Projeto e a Especificação de Requisitos do AgroCiclo.

## 1. Diagnóstico do repositório recebido

O repositório original continha principalmente páginas HTML/CSS de protótipo, sem backend de domínio, persistência, autenticação ou integração dos formulários.

## 2. Estado após a implementação inicial

| Área | Estado atual | Limite conhecido |
|---|---|---|
| Estrutura | Projeto Maven modular, documentação e CI | CI ainda não executada nesta sessão |
| Backend | Spring Boot MVC, JPA, validação e serviços do Incremento 1 | Incrementos 2 e 3 pendentes |
| Banco | PostgreSQL e primeira migração Flyway | Migração ainda precisa ser executada em PostgreSQL real |
| Segurança | Login/senha com sessão, BCrypt, perfis e token de setup de uso único | Método definitivo depende de D05; testes de segurança completos pendentes |
| Isolamento | Consultas iniciais filtradas pela propriedade do usuário | Modelo inicial vincula conta a uma propriedade; multi-propriedade depende de decisão |
| Cadastros | Cargo, funcionário, talhão, pastagem, lote e animal | Edição, arquivamento e exclusão segura ainda não expostos |
| Pecuária | Vínculo temporal animal-lote e transferência | Pesagens, sanidade, produção e movimentação de pastagem pendentes |
| Auditoria | Registra inclusão, transferência e desativação; consulta por período/tipo | Cobertura de todos os eventos ainda pendente |
| Frontend | Telas originais preservadas; seis formulários integrados ao núcleo | Demais telas continuam como protótipo |
| Testes | Testes de contexto e configuração inicial adicionados | Não executados porque Maven/dependências não puderam ser baixados |

A validação executada localmente nesta revisão foi estática: inspeção de arquivos, revisão de estrutura, XML e tentativa de iniciar o Maven Wrapper. A tentativa falhou por indisponibilidade de resolução DNS para `repo.maven.apache.org`; portanto não há alegação de compilação ou testes aprovados.

## 3. Lacunas do plano que precisam de decisão ou detalhamento

O plano é uma boa base de escopo e requisitos, mas alguns itens permanecem explicitamente pendentes. Outros precisam de detalhamento técnico para que a equipe consiga implementar e testar sem interpretações divergentes.

### 3.1 Decisões já registradas como pendentes no próprio plano

| ID | Tema | O que falta decidir |
|---|---|---|
| D01 | Propriedade piloto | Fazenda parceira ou cliente simulado e perfil produtivo |
| D02 | Hospedagem | Provedor, custo, implantação e HTTPS |
| D03 | Versões/configurações | Matriz definitiva de versões e configurações das tecnologias |
| D04 | Relacionamentos pecuários | Modelo validado de animais, lotes e pastagens |
| D05 | Autenticação | Método definitivo de autenticação |
| D06 | Licença | Licença do código-fonte |
| D07 | LGPD/retenção | Bases legais, prazos de retenção e anonimização/exclusão |
| D08 | Evoluções futuras | Confirmar offline, IoT e integrações fiscais/governamentais fora desta versão |
| D09 | Operação futura | Quem mantém e dá suporte após a disciplina |
| D10 | Backup | Periodicidade dos testes de restauração |
| D11 | Desempenho | Ambiente, conexão e conjunto de dados para os testes |
| D12 | Liderança | Indicar líder do projeto |

O documento também aponta a validação dos parâmetros técnicos de carência e sanidade com consultor (DP03) como dependência. O sistema não deve inventar recomendações veterinárias ou agronômicas.

### 3.2 Detalhamentos técnicos recomendados

1. **Contrato da API:** definir padrão de URLs, métodos HTTP, formato JSON, paginação, filtros, ordenação e versionamento. Registrar exemplos de requisição e resposta por caso de uso.
2. **Modelo relacional:** produzir DER e dicionário de dados, incluindo cardinalidades, chaves, índices, unicidade por propriedade e histórico temporal. A modelagem deve seguir os RF/RD e casos de uso.
3. **Matriz de autorização executável:** transformar a matriz de atores em regras verificadas no servidor e em testes para cada endpoint. Não basta esconder links na interface.
4. **Isolamento multi-propriedade:** definir como a propriedade ativa é selecionada e garantir que todas as consultas, relatórios, exportações e operações filtrem pelo vínculo autorizado.
5. **Transações e concorrência:** especificar como evitar saldo negativo, baixa duplicada de estoque, aplicação parcialmente registrada, compra duplicada por reenvio e conflito entre usuários.
6. **Auditoria e histórico:** definir campos, eventos auditados, proteção contra alteração indevida, consulta e política de retenção.
7. **Erros e validação:** padronizar erros da API, mensagens em português, validação de campos e tratamento de exceções.
8. **Relatórios e exportações:** definir filtros, colunas, totais, formatos e bibliotecas para PDF/CSV. A exportação deve respeitar perfil e propriedade.
9. **Alertas e e-mail:** escolher serviço SMTP, configurar credenciais fora do repositório, definir tentativas, falhas, duplicidade e registro de envio.
10. **Observabilidade:** definir logs, níveis, identificador de requisição, métricas e procedimento para diagnosticar falhas sem expor dados pessoais.
11. **Migração e dados de demonstração:** definir formato de importação, saneamento, validação e conjunto fictício para demonstração e testes.
12. **Acessibilidade e usabilidade:** além da responsividade, definir critérios mínimos de contraste, navegação por teclado, rótulos, mensagens e tamanho de alvos de toque.

### 3.3 Regras de domínio que exigem cuidado especial

- **Estoque:** partidas/lotes, validade, unidade de medida, origem da compra, descarte versus consumo e bloqueio de saldo negativo.
- **Custos:** custo do consumo deve usar o valor unitário da compra de origem; mão de obra e máquinas devem preservar taxa/duração/uso efetivos. Evitar contabilizar duas vezes compra, pagamento e consumo.
- **Pecuária:** animal pertence a um lote por vez; transferências preservam origem, destino, data e histórico.
- **Agricultura:** talhão pertence a uma propriedade; safras e operações precisam estar vinculadas ao talhão e ao período.
- **Sanidade:** parâmetros de carência e calendário precisam de fonte técnica validada; sem parâmetro, o sistema deve indicar pendência e não liberar o produto como seguro.
- **Exclusão:** cadastro com histórico ou vínculos não deve ser apagado. O plano prevê bloquear exclusão nesses casos; avaliar desativação/arquivamento quando apropriado.

## 4. Prioridade recomendada

**Bloqueadores antes de desenvolver funcionalidades reais:**
- Resolver D03 e fixar versões.
- Aprovar D04 e o DER inicial.
- Resolver D05 e implementar autenticação/autorização.
- Definir isolamento por propriedade e matriz de permissões.
- Definir D07 antes de usar dados pessoais reais.
- Especificar transações de estoque, compras e custos.

**Antes de homologação/produção:**
- Resolver D02 e HTTPS.
- Resolver D10 e testar restauração real.
- Resolver D11 e executar testes de desempenho.
- Implementar auditoria, logs, monitoramento e tratamento de falhas.
- Concluir testes de segurança, UAT e documentação de operação.

## 5. Limites desta entrega

Esta organização prepara a base do projeto, mas **não entrega o AgroCiclo funcional**. Ainda faltam entidades, migrações, endpoints, regras de negócio, autenticação, autorização, integração dos formulários, relatórios e testes dos casos de uso. O próximo trabalho deve começar pelo Incremento 1 e ser rastreado aos RF/RD e UCs do documento.
