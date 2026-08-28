-- Categorias iniciais do demo.
--
-- Elas passaram a nascer aqui porque escrever em categoria virou operação de dono (ver
-- SecurityConfig): a lista é única e compartilhada por todos os visitantes, então deixá-la aberta
-- significava que qualquer um podia enchê-la — ou apagá-la — para todo mundo.
--
-- Categoria é cenário, não dado do usuário: quem chega para testar precisa escolher uma, não criar.
--
-- ON CONFLICT DO NOTHING porque cor e fator_ordem são UNIQUE: rodar isto sobre uma base que já
-- tenha alguma dessas categorias não pode quebrar a migração.

INSERT INTO categoria (nome, cor, fator_ordem) VALUES
    ('Saúde',    '#43A047', 1),
    ('Trabalho', '#1E88E5', 2),
    ('Casa',     '#FB8C00', 3),
    ('Estudos',  '#8E24AA', 4),
    ('Pessoal',  '#546E7A', 5)
ON CONFLICT DO NOTHING;
