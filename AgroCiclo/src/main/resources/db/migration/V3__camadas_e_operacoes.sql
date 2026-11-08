-- Evolução aditiva do esquema anterior; V1 e V2 permanecem intactas.

ALTER TABLE cargos RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE funcionarios RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE funcionarios RENAME COLUMN cargo_id TO cod_cargo;

ALTER TABLE usuarios RENAME COLUMN funcionario_id TO cod_funcionario;

ALTER TABLE talhoes RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE pastagens RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE lotes RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE lotes RENAME COLUMN pastagem_id TO cod_pastagem;

ALTER TABLE animais RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE animais RENAME COLUMN lote_atual_id TO cod_lote;

ALTER TABLE vinculos_animal_lote RENAME COLUMN animal_id TO cod_animal;

ALTER TABLE vinculos_animal_lote RENAME COLUMN lote_id TO cod_lote;

ALTER TABLE auditorias RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE pesagens_animais RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE pesagens_animais RENAME COLUMN animal_id TO cod_animal;

ALTER TABLE safras RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE safras RENAME COLUMN talhao_id TO cod_talhao;

ALTER TABLE itens_estoque RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE partidas_estoque RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE partidas_estoque RENAME COLUMN item_estoque_id TO cod_item_estoque;

ALTER TABLE movimentacoes_estoque RENAME COLUMN propriedade_id TO cod_fazenda;

ALTER TABLE movimentacoes_estoque RENAME COLUMN partida_id TO cod_partida;

ALTER TABLE animais ADD COLUMN IF NOT EXISTS dta_nascimento DATE;

ALTER TABLE vinculos_animal_lote ADD COLUMN IF NOT EXISTS cod_fazenda BIGINT;

CREATE TABLE movimentacoes_pastagem (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_lote BIGINT NOT NULL,
    cod_origem BIGINT,
    cod_destino BIGINT NOT NULL,
    dta_operacao DATE NOT NULL
);

ALTER TABLE pesagens_animais ADD COLUMN IF NOT EXISTS cod_lote BIGINT;

ALTER TABLE pesagens_animais ADD COLUMN IF NOT EXISTS composicao VARCHAR(2000);

CREATE TABLE planos_manejo (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_safra BIGINT NOT NULL,
    operacoes VARCHAR(2000) NOT NULL,
    dta_inicio DATE NOT NULL,
    dta_fim DATE NOT NULL,
    versao INTEGER DEFAULT 1
);

CREATE TABLE etapas_cultivo (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_safra BIGINT NOT NULL,
    etapa VARCHAR(2000) NOT NULL,
    dta_operacao DATE NOT NULL,
    observacoes VARCHAR(2000)
);

CREATE TABLE producoes_colheita (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_safra BIGINT NOT NULL,
    dta_operacao DATE NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    unidade VARCHAR(2000) NOT NULL,
    justificativa_perda VARCHAR(2000),
    parcial BOOLEAN DEFAULT TRUE
);

CREATE TABLE manejos (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_safra BIGINT NOT NULL,
    operacao VARCHAR(2000) NOT NULL,
    dta_operacao DATE NOT NULL,
    cod_responsavel BIGINT NOT NULL
);

CREATE TABLE fornecedores (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    identificacao VARCHAR(2000) NOT NULL,
    contato VARCHAR(2000) NOT NULL
);

CREATE TABLE compras (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_fornecedor BIGINT NOT NULL,
    referencia VARCHAR(2000) NOT NULL,
    dta_operacao DATE NOT NULL,
    total NUMERIC(19,4) NOT NULL
);

CREATE TABLE itens_compra (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_compra BIGINT NOT NULL,
    cod_item BIGINT NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    unidade VARCHAR(2000) NOT NULL,
    valor_unitario NUMERIC(19,4) NOT NULL,
    codigo_lote VARCHAR(2000),
    dta_validade DATE
);

ALTER TABLE partidas_estoque ADD COLUMN IF NOT EXISTS cod_compra BIGINT;

ALTER TABLE partidas_estoque ADD COLUMN IF NOT EXISTS referencia_origem VARCHAR(2000);

ALTER TABLE movimentacoes_estoque ADD COLUMN IF NOT EXISTS cod_safra BIGINT;

ALTER TABLE movimentacoes_estoque ADD COLUMN IF NOT EXISTS cod_lote BIGINT;

ALTER TABLE movimentacoes_estoque ADD COLUMN IF NOT EXISTS cod_tarefa BIGINT;

ALTER TABLE movimentacoes_estoque ADD COLUMN IF NOT EXISTS referencia_origem VARCHAR(2000);

ALTER TABLE movimentacoes_estoque ADD COLUMN IF NOT EXISTS cod_responsavel BIGINT;

CREATE TABLE aplicacoes (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_partida BIGINT NOT NULL,
    cod_safra BIGINT,
    cod_animal BIGINT,
    cod_lote BIGINT,
    cod_tarefa BIGINT,
    cod_manejo BIGINT,
    cod_agendamento BIGINT,
    dta_operacao DATE NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    unidade VARCHAR(2000) NOT NULL,
    cod_responsavel BIGINT NOT NULL,
    dosagem VARCHAR(2000),
    orientacao_tecnica VARCHAR(2000),
    carencia_dias INTEGER,
    custo NUMERIC(19,4),
    referencia VARCHAR(2000) NOT NULL
);

CREATE TABLE calendarios_sanitarios (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    especie VARCHAR(2000),
    cod_lote BIGINT,
    cod_produto BIGINT NOT NULL,
    acao VARCHAR(2000) NOT NULL,
    orientacao_tecnica VARCHAR(2000) NOT NULL,
    dta_prevista DATE,
    intervalo_dias INTEGER
);

CREATE TABLE agendamentos_sanitarios (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_calendario BIGINT NOT NULL,
    cod_animal BIGINT,
    cod_lote BIGINT,
    cod_responsavel BIGINT NOT NULL,
    dta_prevista DATE NOT NULL,
    concluido BOOLEAN DEFAULT FALSE
);

CREATE TABLE producoes_pecuarias (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_animal BIGINT,
    cod_lote BIGINT,
    produto VARCHAR(2000) NOT NULL,
    dta_operacao DATE NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    unidade VARCHAR(2000) NOT NULL,
    destinacao VARCHAR(2000),
    aviso_carencia VARCHAR(2000)
);

CREATE TABLE maquinas (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    identificacao VARCHAR(2000) NOT NULL,
    modelo VARCHAR(2000) NOT NULL,
    ano INTEGER NOT NULL,
    chassi VARCHAR(2000),
    abastecivel BOOLEAN DEFAULT FALSE,
    unidade_medidor VARCHAR(2000),
    leitura NUMERIC(19,4) DEFAULT 0,
    dta_leitura DATE
);

CREATE TABLE planos_manutencao (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_maquina BIGINT NOT NULL,
    servico VARCHAR(2000) NOT NULL,
    dta_prevista DATE,
    limite_medidor NUMERIC(19,4),
    concluido BOOLEAN DEFAULT FALSE
);

CREATE TABLE manutencoes (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_maquina BIGINT NOT NULL,
    cod_plano BIGINT,
    tipo VARCHAR(2000) NOT NULL,
    servico VARCHAR(2000) NOT NULL,
    pecas VARCHAR(2000),
    dta_operacao DATE NOT NULL,
    custo NUMERIC(19,4) NOT NULL,
    referencia VARCHAR(2000) NOT NULL,
    cod_safra BIGINT,
    cod_lote BIGINT,
    cod_tarefa BIGINT,
    cod_lancamento BIGINT
);

CREATE TABLE abastecimentos (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_maquina BIGINT NOT NULL,
    dta_operacao DATE NOT NULL,
    litros NUMERIC(19,4) NOT NULL,
    custo NUMERIC(19,4) NOT NULL,
    leitura NUMERIC(19,4),
    referencia VARCHAR(2000) NOT NULL,
    cod_safra BIGINT,
    cod_lote BIGINT,
    cod_tarefa BIGINT,
    cod_lancamento BIGINT
);

CREATE TABLE usos_maquina (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_maquina BIGINT NOT NULL,
    dta_operacao DATE NOT NULL,
    leitura NUMERIC(19,4) NOT NULL,
    intervalo_uso NUMERIC(19,4),
    cod_safra BIGINT,
    cod_lote BIGINT,
    cod_tarefa BIGINT
);

CREATE TABLE tarefas (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_executante BIGINT NOT NULL,
    cod_safra BIGINT,
    cod_lote BIGINT,
    atividade VARCHAR(2000) NOT NULL,
    local VARCHAR(2000) NOT NULL,
    dta_inicio_previsto TIMESTAMP NOT NULL,
    dta_fim_previsto TIMESTAMP NOT NULL,
    status VARCHAR(2000) DEFAULT 'PENDENTE',
    dta_inicio_real TIMESTAMP,
    dta_fim_real TIMESTAMP,
    taxa_preservada NUMERIC(19,4),
    duracao_horas NUMERIC(19,4),
    custo_mao_obra NUMERIC(19,4),
    observacoes VARCHAR(2000)
);

CREATE TABLE lancamentos (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    tipo VARCHAR(2000) NOT NULL,
    dta_operacao DATE NOT NULL,
    descricao VARCHAR(2000) NOT NULL,
    categoria VARCHAR(2000) NOT NULL,
    referencia VARCHAR(2000) NOT NULL,
    emitente VARCHAR(2000) NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    valor_liquidado NUMERIC(19,4) DEFAULT 0,
    cod_safra BIGINT,
    cod_lote BIGINT,
    cod_tarefa BIGINT,
    compra_estoque BOOLEAN DEFAULT FALSE,
    aviso_carencia VARCHAR(2000)
);

CREATE TABLE liquidacoes (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_lancamento BIGINT NOT NULL,
    dta_operacao DATE NOT NULL,
    valor NUMERIC(19,4) NOT NULL,
    comprovante VARCHAR(2000) NOT NULL
);

CREATE TABLE configuracoes_alerta (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    dias_vacina INTEGER NOT NULL,
    dias_validade INTEGER NOT NULL,
    dias_manutencao INTEGER NOT NULL,
    margem_horas NUMERIC(19,4) NOT NULL,
    margem_km NUMERIC(19,4) NOT NULL
);

CREATE TABLE versoes_limites (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_talhao BIGINT NOT NULL,
    descricao_limites VARCHAR(2000),
    dta_registro TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE versoes_planejamento (
    id BIGSERIAL PRIMARY KEY,
    cod_fazenda BIGINT NOT NULL,
    cod_plano BIGINT NOT NULL,
    operacoes VARCHAR(2000) NOT NULL,
    dta_inicio DATE NOT NULL,
    dta_fim DATE NOT NULL,
    versao INTEGER NOT NULL
);

ALTER TABLE vinculos_animal_lote ADD CONSTRAINT fk_vinculos_animal_lote_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE movimentacoes_pastagem ADD CONSTRAINT fk_movimentacoes_pastagem_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE movimentacoes_pastagem ADD CONSTRAINT fk_movimentacoes_pastagem_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE movimentacoes_pastagem ADD CONSTRAINT fk_movimentacoes_pastagem_cod_origem FOREIGN KEY (cod_origem) REFERENCES pastagens(id);

ALTER TABLE movimentacoes_pastagem ADD CONSTRAINT fk_movimentacoes_pastagem_cod_destino FOREIGN KEY (cod_destino) REFERENCES pastagens(id);

ALTER TABLE pesagens_animais ADD CONSTRAINT fk_pesagens_animais_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE planos_manejo ADD CONSTRAINT fk_planos_manejo_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE planos_manejo ADD CONSTRAINT fk_planos_manejo_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE etapas_cultivo ADD CONSTRAINT fk_etapas_cultivo_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE etapas_cultivo ADD CONSTRAINT fk_etapas_cultivo_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE producoes_colheita ADD CONSTRAINT fk_producoes_colheita_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE producoes_colheita ADD CONSTRAINT fk_producoes_colheita_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE manejos ADD CONSTRAINT fk_manejos_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE manejos ADD CONSTRAINT fk_manejos_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE manejos ADD CONSTRAINT fk_manejos_cod_responsavel FOREIGN KEY (cod_responsavel) REFERENCES funcionarios(id);

ALTER TABLE fornecedores ADD CONSTRAINT fk_fornecedores_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE compras ADD CONSTRAINT fk_compras_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE compras ADD CONSTRAINT fk_compras_cod_fornecedor FOREIGN KEY (cod_fornecedor) REFERENCES fornecedores(id);

ALTER TABLE itens_compra ADD CONSTRAINT fk_itens_compra_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE itens_compra ADD CONSTRAINT fk_itens_compra_cod_compra FOREIGN KEY (cod_compra) REFERENCES compras(id);

ALTER TABLE itens_compra ADD CONSTRAINT fk_itens_compra_cod_item FOREIGN KEY (cod_item) REFERENCES itens_estoque(id);

ALTER TABLE partidas_estoque ADD CONSTRAINT fk_partidas_estoque_cod_compra FOREIGN KEY (cod_compra) REFERENCES compras(id);

ALTER TABLE movimentacoes_estoque ADD CONSTRAINT fk_movimentacoes_estoque_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE movimentacoes_estoque ADD CONSTRAINT fk_movimentacoes_estoque_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE movimentacoes_estoque ADD CONSTRAINT fk_movimentacoes_estoque_cod_tarefa FOREIGN KEY (cod_tarefa) REFERENCES tarefas(id);

ALTER TABLE movimentacoes_estoque ADD CONSTRAINT fk_movimentacoes_estoque_cod_responsavel FOREIGN KEY (cod_responsavel) REFERENCES funcionarios(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_partida FOREIGN KEY (cod_partida) REFERENCES partidas_estoque(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_animal FOREIGN KEY (cod_animal) REFERENCES animais(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_tarefa FOREIGN KEY (cod_tarefa) REFERENCES tarefas(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_manejo FOREIGN KEY (cod_manejo) REFERENCES manejos(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_agendamento FOREIGN KEY (cod_agendamento) REFERENCES agendamentos_sanitarios(id);

ALTER TABLE aplicacoes ADD CONSTRAINT fk_aplicacoes_cod_responsavel FOREIGN KEY (cod_responsavel) REFERENCES funcionarios(id);

ALTER TABLE calendarios_sanitarios ADD CONSTRAINT fk_calendarios_sanitarios_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE calendarios_sanitarios ADD CONSTRAINT fk_calendarios_sanitarios_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE calendarios_sanitarios ADD CONSTRAINT fk_calendarios_sanitarios_cod_produto FOREIGN KEY (cod_produto) REFERENCES itens_estoque(id);

ALTER TABLE agendamentos_sanitarios ADD CONSTRAINT fk_agendamentos_sanitarios_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE agendamentos_sanitarios ADD CONSTRAINT fk_agendamentos_sanitarios_cod_calendario FOREIGN KEY (cod_calendario) REFERENCES calendarios_sanitarios(id);

ALTER TABLE agendamentos_sanitarios ADD CONSTRAINT fk_agendamentos_sanitarios_cod_animal FOREIGN KEY (cod_animal) REFERENCES animais(id);

ALTER TABLE agendamentos_sanitarios ADD CONSTRAINT fk_agendamentos_sanitarios_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE agendamentos_sanitarios ADD CONSTRAINT fk_agendamentos_sanitarios_cod_responsavel FOREIGN KEY (cod_responsavel) REFERENCES funcionarios(id);

ALTER TABLE producoes_pecuarias ADD CONSTRAINT fk_producoes_pecuarias_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE producoes_pecuarias ADD CONSTRAINT fk_producoes_pecuarias_cod_animal FOREIGN KEY (cod_animal) REFERENCES animais(id);

ALTER TABLE producoes_pecuarias ADD CONSTRAINT fk_producoes_pecuarias_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE maquinas ADD CONSTRAINT fk_maquinas_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE planos_manutencao ADD CONSTRAINT fk_planos_manutencao_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE planos_manutencao ADD CONSTRAINT fk_planos_manutencao_cod_maquina FOREIGN KEY (cod_maquina) REFERENCES maquinas(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_maquina FOREIGN KEY (cod_maquina) REFERENCES maquinas(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_plano FOREIGN KEY (cod_plano) REFERENCES planos_manutencao(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_tarefa FOREIGN KEY (cod_tarefa) REFERENCES tarefas(id);

ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencoes_cod_lancamento FOREIGN KEY (cod_lancamento) REFERENCES lancamentos(id);

ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimentos_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimentos_cod_maquina FOREIGN KEY (cod_maquina) REFERENCES maquinas(id);

ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimentos_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimentos_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimentos_cod_tarefa FOREIGN KEY (cod_tarefa) REFERENCES tarefas(id);

ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimentos_cod_lancamento FOREIGN KEY (cod_lancamento) REFERENCES lancamentos(id);

ALTER TABLE usos_maquina ADD CONSTRAINT fk_usos_maquina_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE usos_maquina ADD CONSTRAINT fk_usos_maquina_cod_maquina FOREIGN KEY (cod_maquina) REFERENCES maquinas(id);

ALTER TABLE usos_maquina ADD CONSTRAINT fk_usos_maquina_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE usos_maquina ADD CONSTRAINT fk_usos_maquina_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE usos_maquina ADD CONSTRAINT fk_usos_maquina_cod_tarefa FOREIGN KEY (cod_tarefa) REFERENCES tarefas(id);

ALTER TABLE tarefas ADD CONSTRAINT fk_tarefas_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE tarefas ADD CONSTRAINT fk_tarefas_cod_executante FOREIGN KEY (cod_executante) REFERENCES funcionarios(id);

ALTER TABLE tarefas ADD CONSTRAINT fk_tarefas_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE tarefas ADD CONSTRAINT fk_tarefas_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE lancamentos ADD CONSTRAINT fk_lancamentos_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE lancamentos ADD CONSTRAINT fk_lancamentos_cod_safra FOREIGN KEY (cod_safra) REFERENCES safras(id);

ALTER TABLE lancamentos ADD CONSTRAINT fk_lancamentos_cod_lote FOREIGN KEY (cod_lote) REFERENCES lotes(id);

ALTER TABLE lancamentos ADD CONSTRAINT fk_lancamentos_cod_tarefa FOREIGN KEY (cod_tarefa) REFERENCES tarefas(id);

ALTER TABLE liquidacoes ADD CONSTRAINT fk_liquidacoes_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE liquidacoes ADD CONSTRAINT fk_liquidacoes_cod_lancamento FOREIGN KEY (cod_lancamento) REFERENCES lancamentos(id);

ALTER TABLE configuracoes_alerta ADD CONSTRAINT fk_configuracoes_alerta_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE versoes_limites ADD CONSTRAINT fk_versoes_limites_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE versoes_limites ADD CONSTRAINT fk_versoes_limites_cod_talhao FOREIGN KEY (cod_talhao) REFERENCES talhoes(id);

ALTER TABLE versoes_planejamento ADD CONSTRAINT fk_versoes_planejamento_cod_fazenda FOREIGN KEY (cod_fazenda) REFERENCES propriedades(id);

ALTER TABLE versoes_planejamento ADD CONSTRAINT fk_versoes_planejamento_cod_plano FOREIGN KEY (cod_plano) REFERENCES planos_manejo(id);

UPDATE vinculos_animal_lote v SET cod_fazenda = a.cod_fazenda FROM animais a WHERE a.id = v.cod_animal;
ALTER TABLE vinculos_animal_lote ALTER COLUMN cod_fazenda SET NOT NULL;
ALTER TABLE pesagens_animais ALTER COLUMN cod_animal DROP NOT NULL;
UPDATE pesagens_animais p SET cod_lote = v.cod_lote, composicao = CAST(p.cod_animal AS VARCHAR)
FROM vinculos_animal_lote v WHERE v.cod_animal = p.cod_animal AND p.data_pesagem >= v.data_inicio
AND (v.data_fim IS NULL OR p.data_pesagem <= v.data_fim);
CREATE TABLE usuarios_fazendas (
    cod_usuario BIGINT NOT NULL REFERENCES usuarios(id),
    cod_fazenda BIGINT NOT NULL REFERENCES propriedades(id),
    PRIMARY KEY (cod_usuario, cod_fazenda)
);
INSERT INTO usuarios_fazendas(cod_usuario, cod_fazenda)
SELECT u.id, f.cod_fazenda FROM usuarios u JOIN funcionarios f ON f.id = u.cod_funcionario;
ALTER TABLE auditorias ADD COLUMN solicitacao VARCHAR(200);
CREATE TABLE trava_setup (id INTEGER PRIMARY KEY);
INSERT INTO trava_setup VALUES (1);
CREATE TABLE solicitacoes (
    id BIGSERIAL PRIMARY KEY, cod_fazenda BIGINT NOT NULL REFERENCES propriedades(id),
    cod_usuario BIGINT NOT NULL REFERENCES usuarios(id), chave VARCHAR(200) NOT NULL,
    impressao VARCHAR(64) NOT NULL, perfil VARCHAR(30) NOT NULL,
    status INTEGER NOT NULL, resposta TEXT, dta_operacao TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(cod_fazenda, cod_usuario, chave)
);

CREATE UNIQUE INDEX uq_cargos_ident_lower ON cargos(cod_fazenda, lower(nome));

CREATE UNIQUE INDEX uq_talhoes_ident_lower ON talhoes(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_pastagens_ident_lower ON pastagens(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_lotes_ident_lower ON lotes(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_animais_ident_lower ON animais(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_safras_ident_lower ON safras(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_itens_estoque_ident_lower ON itens_estoque(cod_fazenda, lower(nome));

CREATE UNIQUE INDEX uq_fornecedores_ident_lower ON fornecedores(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_maquinas_ident_lower ON maquinas(cod_fazenda, lower(identificacao));

CREATE UNIQUE INDEX uq_usuario_login_lower ON usuarios(lower(login));

CREATE UNIQUE INDEX uq_config_fazenda ON configuracoes_alerta(cod_fazenda);

CREATE UNIQUE INDEX uq_compra_origem ON compras(cod_fazenda, cod_fornecedor, referencia);

CREATE UNIQUE INDEX uq_lancamento_origem ON lancamentos(cod_fazenda, tipo, emitente, referencia);

CREATE UNIQUE INDEX uq_liquidacao_comprovante ON liquidacoes(cod_fazenda, comprovante);

CREATE UNIQUE INDEX uq_aplicacao_referencia ON aplicacoes(cod_fazenda, referencia);

CREATE UNIQUE INDEX uq_movimento_origem ON movimentacoes_estoque(cod_fazenda, referencia_origem) WHERE referencia_origem IS NOT NULL;

CREATE UNIQUE INDEX uq_abastecimento_origem ON abastecimentos(cod_fazenda, referencia);

CREATE UNIQUE INDEX uq_abastecimento_lancamento ON abastecimentos(cod_lancamento) WHERE cod_lancamento IS NOT NULL;

CREATE UNIQUE INDEX uq_manutencao_lancamento ON manutencoes(cod_lancamento) WHERE cod_lancamento IS NOT NULL;

CREATE UNIQUE INDEX uq_manutencao_plano ON manutencoes(cod_plano) WHERE cod_plano IS NOT NULL;

CREATE UNIQUE INDEX uq_aplicacao_agendamento ON aplicacoes(cod_agendamento) WHERE cod_agendamento IS NOT NULL;

ALTER TABLE lancamentos ADD CONSTRAINT ck_lancamento_saldo CHECK (valor_liquidado >= 0 AND valor_liquidado <= valor);

ALTER TABLE producoes_colheita ADD CONSTRAINT ck_producao_quantidade CHECK (quantidade >= 0);

ALTER TABLE liquidacoes ADD CONSTRAINT ck_liquidacao_valor CHECK (valor > 0);
