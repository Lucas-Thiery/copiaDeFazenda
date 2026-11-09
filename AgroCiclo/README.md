# AgroCiclo — Java 21 / Spring Boot

Backend reorganizado nas camadas `Entity`, `DTO`, `Repository`, `Service` e `Controller`, em `com.ufs.gestaofazenda`.

**Leia [RELATORIO_ENTREGA.md](RELATORIO_ENTREGA.md)** para funcionalidades verificadas, limitações e rastreabilidade. A entrega implementa APIs dos três incrementos. As telas originais foram preservadas e apenas parte delas está integrada; o sistema completo ainda requer aceitação e finalização da interface.

## Executar no Windows (PowerShell)

Instale um **JDK 21 completo** e Docker Desktop. Confira `java -version` e `javac -version`; ambos precisam ser Java 21. Maven é obtido pelo wrapper.

```powershell
Copy-Item .env.example .env
# Edite .env para definir a senha do PostgreSQL.
docker compose up -d db

$env:DB_URL = "jdbc:postgresql://localhost:5432/agrociclo"
$env:DB_USER = "agrociclo"
$env:DB_PASSWORD = "a-mesma-senha-definida-no-env"

.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

O `.env` é lido pelo Docker Compose; as variáveis do Java devem ser configuradas no terminal ou na IDE. No IntelliJ/VS Code, importe o `pom.xml`, selecione JDK 21 e habilite o processamento de anotações Lombok.

Abra `http://localhost:8080/` e crie sua conta de proprietário com e-mail e senha. Depois de entrar, cadastre sua primeira fazenda. Para cadastrar outras, use **Cadastrar fazenda** no painel. O seletor no cabeçalho define qual fazenda está ativa na aba.

## Executar no Linux

```bash
cp .env.example .env
# Configure DB_URL, DB_USER e DB_PASSWORD no ambiente.
docker compose up -d db
./mvnw clean verify
./mvnw spring-boot:run
```

## Contrato da API

- IDs: `Long`, como `animalId`, `usuarioId` e `fazendaId`.
- Referências nos DTOs: `codLoteAtual`, `codFazenda`, `codResponsavel`; coleções como `codFazendas`.
- Datas: prefixo `dta`; valores monetários e medidas usam `BigDecimal`.
- Cada DTO reúne classes internas, por exemplo `AnimalDTO.AnimalRequest` e `AnimalDTO.AnimalResponse`.
- Autenticação por sessão e BCrypt. Para login, envie formulário `login` / `senha` a `POST /api/auth/login`.
- Obtenha CSRF em `GET /api/csrf`; mantenha os cookies e envie o token no cabeçalho indicado.
- Toda gravação autenticada exige `Idempotency-Key`. Preserve a chave e os dados ao reenviar a mesma operação; use outra chave para uma operação nova. A chave não substitui a referência de negócio do documento/operação.
- `X-Fazenda-Id` seleciona uma fazenda vinculada ao usuário. Se ausente, utiliza a fazenda do funcionário. O vínculo é validado no servidor.
- Listagens: `pagina=0&tamanho=100`, com tamanho máximo de 500. Não há medição homologada da meta de dois segundos.
- Cadastros aplicáveis: `POST /api/animais`, `GET /api/animais/{id}` etc. Atualização e exclusão existem apenas para referências editáveis; eventos históricos não têm endpoints de exclusão. O [catálogo](docs/API.md) contém todos os recursos.

## Testes e banco

`./mvnw clean verify` executa os testes. H2 é utilizado nos testes rápidos; no Linux também é iniciado PostgreSQL 18.1 real, temporário, para migrations, validação JPA e duas saídas de estoque concorrentes por HTTP. Esse teste é ignorado em outros sistemas operacionais. O CI incluído executa em Linux / Java 21.

As migrations V1 e V2 do projeto anterior permanecem inalteradas. V3 adapta os relacionamentos sem descartar as tabelas antigas e acrescenta os módulos; V4 acrescenta e-mail. O Hibernate utiliza `validate` em execução normal. Faça backup de uma base existente antes da atualização; nomes duplicados que diferem apenas por maiúsculas/minúsculas precisam ser conciliados para os novos índices, sem remoção automática de dados.

## Documentação

- [Relatório da entrega](RELATORIO_ENTREGA.md)
- [APIs e exemplos](docs/API.md)
- [Matriz de requisitos](docs/MATRIZ_REQUISITOS.md)
- [Testes e evidências](docs/TESTES.md)
- [Decisões e pendências](docs/DECISOES.md)
- `docs/historico/`: documentos anteriores, mantidos como histórico; seus estados não descrevem esta entrega.

SMTP é opcional: configure `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME` e `SPRING_MAIL_PASSWORD` conforme seu provedor. Cadastre o e-mail do usuário via `PUT /api/usuarios/{id}/email`. O endpoint de envio informa aceitação, falha ou resultado não confirmado, sem apagar os alertas e sem prometer entrega na caixa de entrada.
