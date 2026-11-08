-- AgroCiclo - modelo operacional dos incrementos 2 e 3.
-- Parâmetros técnicos veterinários/agronômicos permanecem informados e validados
-- por responsável técnico; o sistema não cria doses, carências ou recomendações.

-- Chaves compostas para impedir vínculos entre registros de propriedades diferentes.
ALTER TABLE propriedades ADD CONSTRAINT uq_propriedades_id_propriedade UNIQUE (id);
ALTER TABLE funcionarios ADD CONSTRAINT uq_funcionarios_id_propriedade UNIQUE (id, propriedade_id);
ALTER TABLE talhoes ADD CONSTRAINT uq_talhoes_id_propriedade UNIQUE (id, propriedade_id);
ALTER TABLE pastagens ADD CONSTRAINT uq_pastagens_id_propriedade UNIQUE (id, propriedade_id);
ALTER TABLE lotes ADD CONSTRAINT uq_lotes_id_propriedade UNIQUE (id, propriedade_id);
ALTER TABLE animais ADD CONSTRAINT uq_animais_id_propriedade UNIQUE (id, propriedade_id);

-- Reforça RD08: animal e lote atual devem pertencer à mesma propriedade.
ALTER TABLE animais DROP CONSTRAINT IF EXISTS animais_lote_atual_id_fkey;
ALTER TABLE animais ADD CONSTRAINT fk_animal_lote_mesma_propriedade
  FOREIGN KEY (lote_atual_id, propriedade_id) REFERENCES lotes(id, propriedade_id);
ALTER TABLE lotes ADD CONSTRAINT fk_lote_pastagem_mesma_propriedade
  FOREIGN KEY (pastagem_id, propriedade_id) REFERENCES pastagens(id, propriedade_id);

-- Incremento 2: pecuária e sanidade.
CREATE TABLE pesagens_animais (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id),
 animal_id BIGINT NOT NULL, data_pesagem DATE NOT NULL, peso_kg NUMERIC(10,3) NOT NULL CHECK(peso_kg > 0),
 observacao VARCHAR(500), registrado_por BIGINT REFERENCES usuarios(id), criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(animal_id, propriedade_id) REFERENCES animais(id, propriedade_id)
);
CREATE INDEX ix_pesagens_animal_data ON pesagens_animais(animal_id, data_pesagem DESC);

CREATE TABLE calendarios_sanitarios (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id),
 nome VARCHAR(120) NOT NULL, especie VARCHAR(60), lote_id BIGINT,
 orientacao_tecnica TEXT NOT NULL, referencia_tecnica VARCHAR(300) NOT NULL,
 ativo BOOLEAN NOT NULL DEFAULT TRUE, criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(lote_id, propriedade_id) REFERENCES lotes(id, propriedade_id),
 CHECK (especie IS NOT NULL OR lote_id IS NOT NULL)
);
CREATE TABLE itens_calendario_sanitario (
 id BIGSERIAL PRIMARY KEY, calendario_id BIGINT NOT NULL REFERENCES calendarios_sanitarios(id) ON DELETE CASCADE,
 nome_acao VARCHAR(160) NOT NULL, intervalo_dias INTEGER CHECK(intervalo_dias IS NULL OR intervalo_dias > 0),
 descricao TEXT, referencia_tecnica VARCHAR(300) NOT NULL
);
CREATE TABLE agendamentos_sanitarios (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id),
 calendario_item_id BIGINT REFERENCES itens_calendario_sanitario(id), animal_id BIGINT, lote_id BIGINT,
 acao VARCHAR(160) NOT NULL, data_prevista DATE NOT NULL, data_realizacao DATE,
 status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK(status IN ('PENDENTE','REALIZADO','CANCELADO')),
 referencia_tecnica VARCHAR(300) NOT NULL, observacao TEXT,
 FOREIGN KEY(animal_id, propriedade_id) REFERENCES animais(id, propriedade_id),
 FOREIGN KEY(lote_id, propriedade_id) REFERENCES lotes(id, propriedade_id),
 CHECK ((animal_id IS NOT NULL) <> (lote_id IS NOT NULL))
);
CREATE INDEX ix_agendamento_sanitario_data ON agendamentos_sanitarios(propriedade_id, data_prevista, status);
CREATE TABLE eventos_sanitarios (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), animal_id BIGINT NOT NULL,
 tipo VARCHAR(20) NOT NULL CHECK(tipo IN ('VACINACAO','TRATAMENTO')),
 produto VARCHAR(160) NOT NULL, data_aplicacao DATE NOT NULL, dose_informada VARCHAR(120),
 via_aplicacao VARCHAR(100), responsavel BIGINT REFERENCES funcionarios(id),
 carencia_dias INTEGER CHECK(carencia_dias IS NULL OR carencia_dias >= 0),
 carencia_ate DATE, parametros_validados BOOLEAN NOT NULL DEFAULT FALSE,
 referencia_tecnica VARCHAR(300), observacoes TEXT,
 FOREIGN KEY(animal_id, propriedade_id) REFERENCES animais(id, propriedade_id),
 CHECK ((parametros_validados = FALSE) OR referencia_tecnica IS NOT NULL)
);
CREATE INDEX ix_eventos_sanitarios_animal_data ON eventos_sanitarios(animal_id, data_aplicacao DESC);
CREATE TABLE producoes_animais (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), lote_id BIGINT NOT NULL,
 produto VARCHAR(100) NOT NULL, quantidade NUMERIC(14,3) NOT NULL CHECK(quantidade >= 0),
 unidade VARCHAR(30) NOT NULL, periodo_inicio DATE NOT NULL, periodo_fim DATE NOT NULL,
 observacao TEXT, CHECK(periodo_fim >= periodo_inicio),
 FOREIGN KEY(lote_id, propriedade_id) REFERENCES lotes(id, propriedade_id)
);
CREATE TABLE movimentacoes_lote_pastagem (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), lote_id BIGINT NOT NULL,
 pastagem_origem_id BIGINT NOT NULL, pastagem_destino_id BIGINT NOT NULL, data_movimentacao DATE NOT NULL,
 responsavel BIGINT REFERENCES funcionarios(id), observacao TEXT,
 FOREIGN KEY(lote_id, propriedade_id) REFERENCES lotes(id, propriedade_id),
 FOREIGN KEY(pastagem_origem_id, propriedade_id) REFERENCES pastagens(id, propriedade_id),
 FOREIGN KEY(pastagem_destino_id, propriedade_id) REFERENCES pastagens(id, propriedade_id),
 CHECK(pastagem_origem_id <> pastagem_destino_id)
);
CREATE INDEX ix_mov_lote_pastagem_data ON movimentacoes_lote_pastagem(lote_id, data_movimentacao DESC);

-- Agricultura: safra, plantio, manejo e colheita.
CREATE TABLE safras (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), talhao_id BIGINT NOT NULL,
 cultura VARCHAR(100) NOT NULL, variedade VARCHAR(100), identificacao VARCHAR(100) NOT NULL,
 data_inicio DATE NOT NULL, data_fim_prevista DATE, data_fim DATE,
 area_plantada_ha NUMERIC(12,4) NOT NULL CHECK(area_plantada_ha > 0), status VARCHAR(20) NOT NULL DEFAULT 'PLANEJADA'
 CHECK(status IN ('PLANEJADA','EM_ANDAMENTO','ENCERRADA','CANCELADA')),
 observacoes TEXT, UNIQUE(propriedade_id, identificacao), UNIQUE(id, propriedade_id),
 FOREIGN KEY(talhao_id, propriedade_id) REFERENCES talhoes(id, propriedade_id),
 CHECK(data_fim_prevista IS NULL OR data_fim_prevista >= data_inicio),
 CHECK(data_fim IS NULL OR data_fim >= data_inicio)
);
CREATE TABLE plantios (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), safra_id BIGINT NOT NULL REFERENCES safras(id),
 data_plantio DATE NOT NULL, area_ha NUMERIC(12,4) NOT NULL CHECK(area_ha > 0),
 quantidade_sementes NUMERIC(14,3), unidade_sementes VARCHAR(30), espacamento VARCHAR(100),
 observacoes TEXT
);
CREATE TABLE manejos_lavoura (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), safra_id BIGINT NOT NULL REFERENCES safras(id),
 data_manejo DATE NOT NULL, tipo VARCHAR(100) NOT NULL, descricao TEXT NOT NULL,
 responsavel BIGINT REFERENCES funcionarios(id), referencia_tecnica VARCHAR(300), observacoes TEXT
);
CREATE INDEX ix_manejo_safra_data ON manejos_lavoura(safra_id, data_manejo DESC);
CREATE TABLE colheitas (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), safra_id BIGINT NOT NULL REFERENCES safras(id),
 data_colheita DATE NOT NULL, quantidade NUMERIC(14,3) NOT NULL CHECK(quantidade >= 0),
 unidade VARCHAR(30) NOT NULL, qualidade VARCHAR(100), destino VARCHAR(160), observacoes TEXT
);

-- Estoque e compras. Compra gera obrigação; recebimento gera partida; pagamento é outro evento.
CREATE TABLE fornecedores (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), nome VARCHAR(160) NOT NULL,
 documento VARCHAR(30), telefone VARCHAR(40), email VARCHAR(160), endereco TEXT, ativo BOOLEAN NOT NULL DEFAULT TRUE,
 UNIQUE(propriedade_id, nome)
);
CREATE TABLE itens_estoque (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), codigo VARCHAR(80), nome VARCHAR(160) NOT NULL,
 categoria VARCHAR(80) NOT NULL, unidade_medida VARCHAR(30) NOT NULL,
 estoque_minimo NUMERIC(14,3) NOT NULL DEFAULT 0 CHECK(estoque_minimo >= 0),
 ativo BOOLEAN NOT NULL DEFAULT TRUE, UNIQUE(propriedade_id, nome), UNIQUE(propriedade_id, codigo)
);
CREATE TABLE compras (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), fornecedor_id BIGINT NOT NULL,
 numero_documento VARCHAR(100), data_compra DATE NOT NULL, data_vencimento DATE,
 valor_total NUMERIC(14,2) NOT NULL CHECK(valor_total >= 0), status VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA'
 CHECK(status IN ('REGISTRADA','RECEBIDA_PARCIAL','RECEBIDA','CANCELADA')),
 observacoes TEXT, FOREIGN KEY(fornecedor_id) REFERENCES fornecedores(id)
);
CREATE TABLE itens_compra (
 id BIGSERIAL PRIMARY KEY, compra_id BIGINT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
 item_estoque_id BIGINT NOT NULL REFERENCES itens_estoque(id), quantidade NUMERIC(14,3) NOT NULL CHECK(quantidade > 0),
 valor_unitario NUMERIC(14,4) NOT NULL CHECK(valor_unitario >= 0), unidade VARCHAR(30) NOT NULL
);
CREATE TABLE partidas_estoque (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), item_estoque_id BIGINT NOT NULL REFERENCES itens_estoque(id),
 item_compra_id BIGINT REFERENCES itens_compra(id), codigo_lote VARCHAR(100), data_entrada DATE NOT NULL,
 data_validade DATE, quantidade_inicial NUMERIC(14,3) NOT NULL CHECK(quantidade_inicial > 0),
 quantidade_atual NUMERIC(14,3) NOT NULL CHECK(quantidade_atual >= 0),
 valor_unitario_origem NUMERIC(14,4) NOT NULL CHECK(valor_unitario_origem >= 0), UNIQUE(id, propriedade_id),
 CHECK(data_validade IS NULL OR data_validade >= data_entrada)
);
CREATE INDEX ix_partida_validade ON partidas_estoque(propriedade_id, data_validade);
CREATE TABLE recebimentos_compra (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), item_compra_id BIGINT NOT NULL REFERENCES itens_compra(id),
 partida_id BIGINT NOT NULL REFERENCES partidas_estoque(id), quantidade_recebida NUMERIC(14,3) NOT NULL CHECK(quantidade_recebida > 0),
 data_recebimento DATE NOT NULL, UNIQUE(item_compra_id, partida_id)
);
CREATE TABLE movimentacoes_estoque (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), partida_id BIGINT NOT NULL,
 tipo VARCHAR(20) NOT NULL CHECK(tipo IN ('ENTRADA','USO','DESCARTE','AJUSTE_ENTRADA','AJUSTE_SAIDA')),
 quantidade NUMERIC(14,3) NOT NULL CHECK(quantidade > 0), data_operacao DATE NOT NULL,
 motivo VARCHAR(300) NOT NULL, atividade_tipo VARCHAR(30), atividade_id BIGINT,
 responsavel BIGINT REFERENCES funcionarios(id), valor_total_origem NUMERIC(14,4), criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_mov_estoque_partida_data ON movimentacoes_estoque(partida_id, data_operacao);
CREATE TABLE aplicacoes_insumos (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), partida_id BIGINT NOT NULL,
 safra_id BIGINT REFERENCES safras(id), animal_id BIGINT REFERENCES animais(id), lote_id BIGINT REFERENCES lotes(id),
 data_aplicacao DATE NOT NULL, quantidade NUMERIC(14,3) NOT NULL CHECK(quantidade > 0), unidade VARCHAR(30) NOT NULL,
 carencia_dias INTEGER CHECK(carencia_dias IS NULL OR carencia_dias >= 0), carencia_ate DATE,
 parametros_validados BOOLEAN NOT NULL DEFAULT FALSE, referencia_tecnica VARCHAR(300), responsavel BIGINT REFERENCES funcionarios(id),
 observacoes TEXT, CHECK ((safra_id IS NOT NULL)::int + (animal_id IS NOT NULL)::int + (lote_id IS NOT NULL)::int = 1),
 CHECK(parametros_validados = FALSE OR referencia_tecnica IS NOT NULL)
);

-- Financeiro: obrigação/lançamento separado de liquidação efetiva (DP-2).
CREATE TABLE lancamentos_financeiros (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), tipo VARCHAR(10) NOT NULL CHECK(tipo IN ('RECEITA','DESPESA')),
 descricao VARCHAR(240) NOT NULL, categoria VARCHAR(100), valor_previsto NUMERIC(14,2) NOT NULL CHECK(valor_previsto >= 0),
 data_competencia DATE NOT NULL, data_vencimento DATE, origem_tipo VARCHAR(30), origem_id BIGINT,
 status VARCHAR(20) NOT NULL DEFAULT 'ABERTO' CHECK(status IN ('ABERTO','PARCIAL','LIQUIDADO','CANCELADO')),
 observacoes TEXT, criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE liquidacoes_financeiras (
 id BIGSERIAL PRIMARY KEY, lancamento_id BIGINT NOT NULL REFERENCES lancamentos_financeiros(id),
 data_liquidacao DATE NOT NULL, valor NUMERIC(14,2) NOT NULL CHECK(valor > 0),
 meio_pagamento VARCHAR(60), comprovante VARCHAR(240), observacoes TEXT, criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_lancamentos_competencia ON lancamentos_financeiros(propriedade_id, data_competencia);
CREATE INDEX ix_liquidacoes_data ON liquidacoes_financeiras(data_liquidacao);
ALTER TABLE compras ADD COLUMN lancamento_financeiro_id BIGINT UNIQUE REFERENCES lancamentos_financeiros(id);
ALTER TABLE compras ADD CONSTRAINT uq_compra_id_propriedade UNIQUE(id, propriedade_id);

-- Pessoas e tarefas: preserva taxa e duração no momento da conclusão.
CREATE TABLE tarefas (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), titulo VARCHAR(160) NOT NULL,
 descricao TEXT, atividade_tipo VARCHAR(30), atividade_id BIGINT, prioridade VARCHAR(15) NOT NULL DEFAULT 'NORMAL'
 CHECK(prioridade IN ('BAIXA','NORMAL','ALTA','URGENTE')),
 status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK(status IN ('PENDENTE','EM_ANDAMENTO','CONCLUIDA','CANCELADA')),
 data_inicio_prevista DATE, data_fim_prevista DATE, data_conclusao DATE, criado_por BIGINT REFERENCES usuarios(id),
 CHECK(data_fim_prevista IS NULL OR data_inicio_prevista IS NULL OR data_fim_prevista >= data_inicio_prevista)
);
CREATE TABLE atribuicoes_tarefa (
 id BIGSERIAL PRIMARY KEY, tarefa_id BIGINT NOT NULL REFERENCES tarefas(id) ON DELETE CASCADE,
 funcionario_id BIGINT NOT NULL REFERENCES funcionarios(id), atribuido_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(tarefa_id, funcionario_id)
);
CREATE TABLE execucoes_tarefa (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), tarefa_id BIGINT NOT NULL REFERENCES tarefas(id),
 funcionario_id BIGINT NOT NULL REFERENCES funcionarios(id), inicio_em TIMESTAMPTZ, fim_em TIMESTAMPTZ,
 duracao_horas NUMERIC(10,3) CHECK(duracao_horas IS NULL OR duracao_horas >= 0),
 taxa_hora_snapshot NUMERIC(12,2) CHECK(taxa_hora_snapshot IS NULL OR taxa_hora_snapshot >= 0),
 custo_mao_obra NUMERIC(14,2) CHECK(custo_mao_obra IS NULL OR custo_mao_obra >= 0),
 observacoes TEXT, CHECK(fim_em IS NULL OR inicio_em IS NULL OR fim_em >= inicio_em)
);

-- Máquinas, medidores e manutenção.
CREATE TABLE maquinas (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), identificacao VARCHAR(100) NOT NULL,
 tipo VARCHAR(100) NOT NULL, marca VARCHAR(100), modelo VARCHAR(100), ano INTEGER CHECK(ano IS NULL OR ano >= 1900),
 unidade_medidor VARCHAR(20) NOT NULL DEFAULT 'HORA', ativa BOOLEAN NOT NULL DEFAULT TRUE,
 UNIQUE(propriedade_id, identificacao), UNIQUE(id, propriedade_id)
);
CREATE TABLE abastecimentos (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), maquina_id BIGINT NOT NULL REFERENCES maquinas(id),
 data_abastecimento DATE NOT NULL, quantidade NUMERIC(14,3) NOT NULL CHECK(quantidade > 0), unidade VARCHAR(30) NOT NULL,
 valor_total NUMERIC(14,2) CHECK(valor_total IS NULL OR valor_total >= 0), fornecedor_id BIGINT REFERENCES fornecedores(id),
 horimetro NUMERIC(14,3) CHECK(horimetro IS NULL OR horimetro >= 0), observacoes TEXT
);
CREATE TABLE leituras_medidor (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), maquina_id BIGINT NOT NULL REFERENCES maquinas(id),
 data_leitura DATE NOT NULL, valor NUMERIC(14,3) NOT NULL CHECK(valor >= 0), unidade VARCHAR(20) NOT NULL,
 UNIQUE(maquina_id, data_leitura)
);
CREATE TABLE planos_manutencao (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), maquina_id BIGINT NOT NULL REFERENCES maquinas(id),
 descricao VARCHAR(240) NOT NULL, criterio VARCHAR(15) NOT NULL CHECK(criterio IN ('DATA','USO')),
 proxima_data DATE, limite_uso NUMERIC(14,3), unidade_medidor VARCHAR(20), margem_alerta NUMERIC(14,3),
 ativo BOOLEAN NOT NULL DEFAULT TRUE, referencia_tecnica VARCHAR(300),
 CHECK((criterio='DATA' AND proxima_data IS NOT NULL AND limite_uso IS NULL) OR
       (criterio='USO' AND limite_uso IS NOT NULL AND unidade_medidor IS NOT NULL AND proxima_data IS NULL))
);
CREATE TABLE manutencoes (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), maquina_id BIGINT NOT NULL REFERENCES maquinas(id),
 plano_id BIGINT REFERENCES planos_manutencao(id), tipo VARCHAR(15) NOT NULL CHECK(tipo IN ('PREVENTIVA','CORRETIVA')),
 data_manutencao DATE NOT NULL, descricao TEXT NOT NULL, custo NUMERIC(14,2) CHECK(custo IS NULL OR custo >= 0),
 horimetro NUMERIC(14,3) CHECK(horimetro IS NULL OR horimetro >= 0), responsavel BIGINT REFERENCES funcionarios(id), observacoes TEXT
);

-- Custos diretamente vinculados: consumo valorado pela origem; sem rateio geral automático.
CREATE TABLE custos_operacionais (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id),
 atividade_tipo VARCHAR(30) NOT NULL CHECK(atividade_tipo IN ('SAFRA','LOTE','TAREFA')),
 atividade_id BIGINT NOT NULL, componente VARCHAR(20) NOT NULL CHECK(componente IN ('INSUMO','MAO_DE_OBRA','MAQUINA','DESPESA_DIRETA')),
 origem_tipo VARCHAR(30) NOT NULL, origem_id BIGINT, quantidade NUMERIC(14,3), unidade VARCHAR(30),
 valor_unitario_snapshot NUMERIC(14,4), valor_total NUMERIC(14,2), data_referencia DATE NOT NULL,
 dados_suficientes BOOLEAN NOT NULL DEFAULT TRUE, observacao_pendencia TEXT,
 CHECK(valor_total IS NULL OR valor_total >= 0),
 CHECK(dados_suficientes = TRUE OR observacao_pendencia IS NOT NULL)
);
CREATE INDEX ix_custo_atividade ON custos_operacionais(propriedade_id, atividade_tipo, atividade_id);

-- Configuração e caixa de alertas. A geração é baseada em datas/limites efetivamente cadastrados.
CREATE TABLE configuracoes_alerta (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL UNIQUE REFERENCES propriedades(id),
 vacina_dias INTEGER CHECK(vacina_dias IS NULL OR vacina_dias >= 0),
 validade_insumo_dias INTEGER CHECK(validade_insumo_dias IS NULL OR validade_insumo_dias >= 0),
 manutencao_data_dias INTEGER CHECK(manutencao_data_dias IS NULL OR manutencao_data_dias >= 0),
 manutencao_uso_margem NUMERIC(14,3) CHECK(manutencao_uso_margem IS NULL OR manutencao_uso_margem >= 0),
 atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE alertas (
 id BIGSERIAL PRIMARY KEY, propriedade_id BIGINT NOT NULL REFERENCES propriedades(id), tipo VARCHAR(30) NOT NULL,
 titulo VARCHAR(180) NOT NULL, mensagem TEXT NOT NULL, entidade_tipo VARCHAR(40), entidade_id BIGINT,
 data_referencia DATE, gerado_em TIMESTAMPTZ NOT NULL DEFAULT now(), lido_em TIMESTAMPTZ,
 status VARCHAR(15) NOT NULL DEFAULT 'ABERTO' CHECK(status IN ('ABERTO','LIDO','RESOLVIDO'))
);
CREATE INDEX ix_alertas_propriedade_status ON alertas(propriedade_id, status, gerado_em DESC);
CREATE TABLE tentativas_email_alerta (
 id BIGSERIAL PRIMARY KEY, alerta_id BIGINT NOT NULL REFERENCES alertas(id), destinatario VARCHAR(200) NOT NULL,
 tentado_em TIMESTAMPTZ NOT NULL DEFAULT now(), enviado BOOLEAN NOT NULL, detalhe_falha TEXT
);

-- Integridade de propriedade nas relações que compartilham o mesmo escopo.
ALTER TABLE safras ADD CONSTRAINT fk_safra_talhao_propriedade FOREIGN KEY(talhao_id, propriedade_id) REFERENCES talhoes(id, propriedade_id);
ALTER TABLE plantios ADD CONSTRAINT fk_plantio_safra_propriedade FOREIGN KEY(safra_id, propriedade_id) REFERENCES safras(id, propriedade_id);
ALTER TABLE manejos_lavoura ADD CONSTRAINT fk_manejo_safra_propriedade FOREIGN KEY(safra_id, propriedade_id) REFERENCES safras(id, propriedade_id);
ALTER TABLE colheitas ADD CONSTRAINT fk_colheita_safra_propriedade FOREIGN KEY(safra_id, propriedade_id) REFERENCES safras(id, propriedade_id);
ALTER TABLE itens_estoque ADD CONSTRAINT uq_item_estoque_id_propriedade UNIQUE(id, propriedade_id);
ALTER TABLE fornecedores ADD CONSTRAINT uq_fornecedor_id_propriedade UNIQUE(id, propriedade_id);
ALTER TABLE compras ADD CONSTRAINT fk_compra_fornecedor_propriedade FOREIGN KEY(fornecedor_id, propriedade_id) REFERENCES fornecedores(id, propriedade_id);
ALTER TABLE partidas_estoque ADD CONSTRAINT fk_partida_item_propriedade FOREIGN KEY(item_estoque_id, propriedade_id) REFERENCES itens_estoque(id, propriedade_id);
ALTER TABLE abastecimentos ADD CONSTRAINT fk_abastecimento_maquina_propriedade FOREIGN KEY(maquina_id, propriedade_id) REFERENCES maquinas(id, propriedade_id);
ALTER TABLE leituras_medidor ADD CONSTRAINT fk_leitura_maquina_propriedade FOREIGN KEY(maquina_id, propriedade_id) REFERENCES maquinas(id, propriedade_id);
ALTER TABLE planos_manutencao ADD CONSTRAINT fk_plano_maquina_propriedade FOREIGN KEY(maquina_id, propriedade_id) REFERENCES maquinas(id, propriedade_id);
ALTER TABLE manutencoes ADD CONSTRAINT fk_manutencao_maquina_propriedade FOREIGN KEY(maquina_id, propriedade_id) REFERENCES maquinas(id, propriedade_id);
ALTER TABLE movimentacoes_estoque ADD CONSTRAINT fk_mov_partida_propriedade FOREIGN KEY(partida_id, propriedade_id) REFERENCES partidas_estoque(id, propriedade_id);
ALTER TABLE aplicacoes_insumos ADD CONSTRAINT fk_aplicacao_partida_propriedade FOREIGN KEY(partida_id, propriedade_id) REFERENCES partidas_estoque(id, propriedade_id);

-- Protege o princípio de que a quantidade em estoque nunca pode ficar negativa.
CREATE OR REPLACE FUNCTION validar_saldo_partida_estoque() RETURNS trigger AS $$
DECLARE saldo NUMERIC(14,3); validade DATE;
BEGIN
 SELECT quantidade_atual, data_validade INTO saldo, validade FROM partidas_estoque WHERE id = NEW.partida_id FOR UPDATE;
 IF NEW.tipo IN ('USO','DESCARTE','AJUSTE_SAIDA') THEN
   IF NEW.quantidade > saldo THEN RAISE EXCEPTION 'Saldo insuficiente na partida %', NEW.partida_id USING ERRCODE='23514'; END IF;
   IF NEW.tipo='USO' AND validade IS NOT NULL AND validade < NEW.data_operacao THEN
     RAISE EXCEPTION 'Partida vencida não pode ser usada' USING ERRCODE='23514';
   END IF;
   UPDATE partidas_estoque SET quantidade_atual = quantidade_atual - NEW.quantidade WHERE id=NEW.partida_id;
 ELSIF NEW.tipo IN ('ENTRADA','AJUSTE_ENTRADA') THEN
   UPDATE partidas_estoque SET quantidade_atual = quantidade_atual + NEW.quantidade WHERE id=NEW.partida_id;
 END IF;
 RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_movimentacao_saldo BEFORE INSERT ON movimentacoes_estoque
FOR EACH ROW EXECUTE FUNCTION validar_saldo_partida_estoque();

-- Evita liquidações superiores ao valor da obrigação, incluindo concorrência.
CREATE OR REPLACE FUNCTION validar_total_liquidacoes() RETURNS trigger AS $$
DECLARE previsto NUMERIC(14,2); liquidado NUMERIC(14,2);
BEGIN
 SELECT valor_previsto INTO previsto FROM lancamentos_financeiros WHERE id=NEW.lancamento_id FOR UPDATE;
 SELECT COALESCE(SUM(valor),0) INTO liquidado FROM liquidacoes_financeiras WHERE lancamento_id=NEW.lancamento_id;
 IF liquidado + NEW.valor > previsto THEN RAISE EXCEPTION 'Liquidação excede saldo pendente' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_validar_liquidacao BEFORE INSERT ON liquidacoes_financeiras
FOR EACH ROW EXECUTE FUNCTION validar_total_liquidacoes();
