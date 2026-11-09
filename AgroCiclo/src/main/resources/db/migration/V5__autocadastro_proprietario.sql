ALTER TABLE usuarios ADD COLUMN nome_proprietario VARCHAR(140);
ALTER TABLE usuarios ALTER COLUMN cod_funcionario DROP NOT NULL;
ALTER TABLE usuarios ALTER COLUMN login TYPE VARCHAR(254);
ALTER TABLE auditorias ALTER COLUMN autor TYPE VARCHAR(254);

ALTER TABLE usuarios ADD CONSTRAINT ck_usuario_vinculo_inicial
    CHECK (cod_funcionario IS NOT NULL OR
           (perfil = 'PROPRIETARIO' AND nome_proprietario IS NOT NULL AND email IS NOT NULL));
