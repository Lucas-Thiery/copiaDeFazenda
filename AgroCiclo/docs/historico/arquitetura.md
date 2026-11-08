# Arquitetura do AgroCiclo

## Visão geral

A aplicação é um monólito modular Spring Boot. A interface HTML/CSS/JavaScript é servida pelo próprio backend; a API REST aplica validação, autorização, regras de domínio e persistência em PostgreSQL.

```text
Navegador
  ├── páginas HTML/CSS/JS (src/main/resources/static/fazendaweb)
  └── /api/*
       ├── Controller  → contrato HTTP e validação de entrada
       ├── Service     → regras de negócio e transações
       ├── Repository  → consultas/persistência JPA
       └── Domain      → entidades e invariantes
                         ↓
                    PostgreSQL
                    (Flyway)
```

## Módulos

- `cadastros`: propriedade, cargo e talhão.
- `seguranca`: usuário, perfil, autenticação, sessão e desativação.
- `pessoas`: funcionário e custo-hora efetivo.
- `pecuaria`: pastagem, lote, animal e histórico de vínculos animal-lote.
- `common`: configuração de segurança, contexto da sessão, auditoria e contratos compartilhados.
- `agricultura`, `estoque`, `financeiro`, `maquinas`, `relatorios` e `alertas`: pacotes preparados; implementação funcional pendente conforme o roadmap.

## Segurança e escopo de dados

- Login por identificador e senha, com senha armazenada em BCrypt e sessão HTTP.
- Perfis fixos: `PROPRIETARIO`, `GERENTE_CAMPO` e `OPERADOR`.
- `@PreAuthorize` protege as operações no servidor.
- O contexto da sessão resolve a propriedade associada ao funcionário da conta; listagens e operações do núcleo filtram por esse identificador.
- Contas, funcionários e propriedades inativos não podem manter operações autenticadas; o filtro verifica a situação da conta em cada requisição.
- CSRF utiliza cookie `XSRF-TOKEN` e cabeçalho `X-XSRF-TOKEN` para chamadas de escrita do frontend.
- A configuração inicial é de uso único, requer `AGRO_CICLO_SETUP_TOKEN` e cria a primeira propriedade e conta Proprietário sem cadastro público.

A escolha final do método de autenticação ainda depende da decisão D05 do plano. A implementação atual é provisória e segue a proposta login/senha descrita no UC09.

## Persistência

- Entidades JPA não criam nem alteram o esquema automaticamente (`ddl-auto=validate`).
- Toda mudança estrutural deve ser adicionada como nova migração SQL Flyway.
- A primeira migração cria núcleo, cadastros, pecuária básica, vínculos históricos e auditoria.
- Testes automatizados usam H2; testes finais de compatibilidade devem executar também contra PostgreSQL.

## Auditoria e histórico

A trilha registra autor, data/hora, tipo de evento, entidade e identificador do registro. Senhas e hashes não são registrados. O vínculo animal-lote é temporal: a transferência encerra o vínculo anterior e cria outro dentro da mesma transação.

## Frontend

As páginas existentes foram preservadas. As telas de cadastro de cargo, funcionário, talhão, pastagem, lote e animal receberam integração inicial com a API. As demais telas ainda podem conter dados demonstrativos, links ou ações de protótipo e não representam funcionalidades implementadas.

## Limites e decisões pendentes

O modelo inicial associa uma conta a um funcionário e, por consequência, a uma propriedade. Suporte completo a múltiplas propriedades por usuário depende de decisão de arquitetura e de D04/RF55. Hospedagem/HTTPS, retenção LGPD, licença, backup e desempenho dependem das decisões D02, D06, D07, D10 e D11. Não utilizar dados pessoais reais nem disponibilizar o sistema em produção antes de fechar essas decisões e executar os testes de segurança.
