-- Database: routine-pusher

-- DROP DATABASE IF EXISTS "routine-pusher";

CREATE DATABASE "routine-pusher"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

-- DROP TABLE IF EXISTS "categoria";

CREATE TABLE public.categoria
(
    id serial NOT NULL,
    nome varchar(25) NOT NULL,
    cor varchar(10) NOT NULL,
    fator_ord int NOT NULL,
    PRIMARY KEY(id)
);
ALTER TABLE IF EXISTS public.categoria
    OWNER to postgres;

-- DROP TABLE IF EXISTS "lembrete";

CREATE TABLE public.lembrete
(
    id serial NOT NULL,
    titulo varchar(25) NOT NULL,
    comentario varchar(100),
    status varchar(14) NOT NULL,
    categoria_id int NOT NULL,
    data_criacao date NOT NULL,
    momento_notifica date,
    repeticao date,
    quantidade int,
    validade date,
	PRIMARY KEY(id)
);
ALTER TABLE IF EXISTS public.lembrete
    OWNER to postgres;

-- DROP TABLE IF EXISTS "tarefa";

CREATE TABLE public.tarefa
(
    id serial NOT NULL,
    titulo varchar(25) NOT NULL,
    status varchar(14) NOT NULL,
    lembrete_id int NOT NULL,
	PRIMARY KEY(id),
    CONSTRAINT fk_lembrete
        FOREIGN KEY (lembrete_id)
        REFERENCES lembrete(id)
        ON DELETE CASCADE
);
ALTER TABLE IF EXISTS public.tarefa
    OWNER to postgres;

SELECT * FROM categoria;
SELECT * FROM lembrete;
SELECT * FROM tarefa;

SELECT t.*, l.descricao AS descricao_lembrete
FROM tarefa t
JOIN lembrete l ON t.lembrete_id = l.id;