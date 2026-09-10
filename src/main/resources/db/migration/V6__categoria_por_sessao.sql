-- Categoria deixa de ser cenário compartilhado e passa a pertencer à sessão do visitante.
--
-- Até aqui a lista era única e global: era por isso que escrever nela virou operação de dono (ver
-- SecurityConfig) e que as categorias nasciam pela V5. Com lista por visitante, quem cria e apaga é
-- o próprio dono da sessão, e os padrões nascem junto com ela — não por migração.

-- 1) As unicidades eram globais e não sobrevivem a listas por usuário: dois visitantes podem, e
--    devem poder, escolher a mesma cor. Elas voltam adiante, agora dentro de cada lista.
ALTER TABLE categoria DROP CONSTRAINT IF EXISTS categoria_cor_key;
ALTER TABLE categoria DROP CONSTRAINT IF EXISTS categoria_fator_ordem_key;

-- 2) Nasce anulável para dar tempo de dar dono ao que já existe.
ALTER TABLE categoria ADD COLUMN sessao_id BIGINT;

-- 3) Toda categoria global em uso vira uma cópia pertencente à sessão que a usa. Sem isto os
--    lembretes existentes ficariam apontando para linhas que o passo 5 apaga.
INSERT INTO categoria (nome, cor, fator_ordem, sessao_id)
SELECT DISTINCT c.nome, c.cor, c.fator_ordem, l.sessao_id
FROM lembrete l
         JOIN categoria c ON c.id = l.categoria_id
WHERE c.sessao_id IS NULL;

-- 4) E os lembretes passam a apontar para a cópia da própria sessão. O casamento é pela cor porque
--    ela era UNIQUE no conjunto global, então identifica a categoria de origem sem ambiguidade.
UPDATE lembrete l
SET categoria_id = nova.id
FROM categoria antiga,
     categoria nova
WHERE antiga.id = l.categoria_id
  AND antiga.sessao_id IS NULL
  AND nova.sessao_id = l.sessao_id
  AND nova.cor = antiga.cor;

-- 5) O que sobrou sem dono é o cenário compartilhado da V5, que não tem mais lugar no modelo.
DELETE FROM categoria WHERE sessao_id IS NULL;

-- 6) Agora dá para exigir dono. A FK não é ON DELETE CASCADE de propósito: quem desmonta a sessão é
--    o SessaoService, que sabe cancelar os agendamentos antes de apagar qualquer coisa.
ALTER TABLE categoria ALTER COLUMN sessao_id SET NOT NULL;
ALTER TABLE categoria
    ADD CONSTRAINT fk_categoria_sessao FOREIGN KEY (sessao_id) REFERENCES sessao_anonima (id);

CREATE INDEX idx_categoria_sessao ON categoria (sessao_id);

-- 7) Unicidade de volta, agora por lista: continua impedindo cor e posição repetidas dentro da
--    lista de um visitante, sem impedir que dois visitantes tenham as mesmas.
ALTER TABLE categoria ADD CONSTRAINT uk_categoria_sessao_cor UNIQUE (sessao_id, cor);
ALTER TABLE categoria ADD CONSTRAINT uk_categoria_sessao_fator_ordem UNIQUE (sessao_id, fator_ordem);

-- 8) Teto de 25 caracteres no nome. O UPDATE antes do ALTER é seguro-morre-cedo: sem ele, um nome
--    longo pré-existente faria a migração falhar no meio, com o schema já parcialmente alterado.
UPDATE categoria SET nome = LEFT(nome, 25) WHERE LENGTH(nome) > 25;
ALTER TABLE categoria ALTER COLUMN nome TYPE VARCHAR(25);
