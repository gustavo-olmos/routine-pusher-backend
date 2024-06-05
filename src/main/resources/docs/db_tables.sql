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

CREATE TABLE public.subtarefa
(
    id serial NOT NULL,
    titulo varchar(25) NOT NULL,
    status varchar(14) NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE IF EXISTS public.subtarefa
    OWNER to postgres;
    
    
CREATE TABLE public.tarefa
(
    id serial NOT NULL,
    subtarefa_id integer NOT NULL,
    titulo varchar(25) NOT NULL,
    comentario varchar(100),
    status varchar(14) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (subtarefa_id) REFERENCES subtarefa(id)
);
ALTER TABLE IF EXISTS public.tarefa
    OWNER to postgres;


CREATE TABLE public.categoria
(
    id serial NOT NULL,
    nome varchar(25) NOT NULL DEFAULT 25,
    cor varchar(10) NOT NULL,
    fator_ord int NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE IF EXISTS public.categoria
    OWNER to postgres;


CREATE TABLE public.lembrete
(
    id serial NOT NULL,
    tarefa_id int NOT NULL,
    categoria_id int NOT NULL,
    data_criacao date NOT NULL,
    momento_notifica date,
    repeticao date,
    quantidade int,
    validade date,
    PRIMARY KEY (id),
    FOREIGN KEY (tarefa_id) REFERENCES tarefa(id)
);
ALTER TABLE IF EXISTS public.lembrete
    OWNER to postgres;