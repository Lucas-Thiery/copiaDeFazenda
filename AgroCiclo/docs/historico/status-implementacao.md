> Atualização em 07/11/2026: consulte `entrega-revisao-2026-11-07.md` para o estado mais recente. Este arquivo descreve o estado anterior.

# Status de implementação

## Implementado nesta revisão

- Base Spring Boot 3.5.7 / Java 21 e Maven Wrapper.
- Configuração PostgreSQL, migração inicial Flyway e perfil H2 para testes.
- Configuração inicial de propriedade e primeiro Proprietário, protegida por segredo de uso único.
- Login por sessão, logout, BCrypt, três perfis, proteção de rotas e invalidação de sessão quando a conta é desativada.
- Núcleo de cadastros: cargo, funcionário, talhão, pastagem, lote e animal.
- Transferência de animal com preservação do histórico de lotes.
- Auditoria básica e consultas iniciais limitadas à propriedade da conta.
- Integração inicial das páginas de cadastro correspondentes.
- Documentação, CI e testes iniciais adicionados.

## Ainda pendente

- Compilação e testes executados em ambiente com acesso ao Maven Central.
- Testes de integração reais com PostgreSQL e testes negativos completos de autorização/isolamento.
- Edição, arquivamento e exclusão condicionada a ausência de histórico.
- Suporte a mais de uma propriedade por conta, caso seja aprovado.
- Incremento 2 completo: manejo pecuário, sanidade, calendário sanitário, agricultura, estoque, compras e consumo.
- Incremento 3 completo: receitas/despesas, liquidações, custos, tarefas, máquinas, manutenção, indicadores, relatórios e alertas.
- Integração das demais páginas do protótipo; remoção de dados e ações demonstrativas.
- Aprovação formal das decisões D01–D12 aplicáveis e política LGPD.
- HTTPS, backup e teste de restauração, análise de vulnerabilidades e testes de desempenho antes de qualquer publicação em produção.

## Validação desta revisão

- XML do `pom.xml`: analisado estruturalmente.
- Código Java: inspeção estática e tentativa de análise sintática com `javac`; sem classpath não é possível confirmar compilação.
- Maven Wrapper: tentativa de execução falhou ao resolver `repo.maven.apache.org` por DNS indisponível no ambiente.
- Não declarar `mvn verify` aprovado até execução local ou na CI.


## Revisão adicional — fundação de dados dos incrementos 2 e 3

Foi elaborado um rascunho SQL para os domínios de manejo pecuário, agricultura, estoque, compras, financeiro, tarefas, máquinas, custos e alertas. Ele está em `docs/sql/` e deliberadamente não está no diretório ativo do Flyway, pois não foi possível executar PostgreSQL para validar o DDL. Nenhum desses módulos deve ser anunciado como funcional até que existam serviços, API, interface e testes. Consulte `docs/implementacao-plano.md` para rastreabilidade RF/RD.
