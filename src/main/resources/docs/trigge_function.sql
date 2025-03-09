CREATE OR REPLACE FUNCTION notify_lembrete_insert()
RETURNS TRIGGER AS $$
DECLARE
    payload TEXT;
BEGIN
    payload := json_build_object(
        'id', NEW.id,
        'titulo', NEW.titulo,
        'descricao', NEW.descricao,
        'data_hora', NEW.data_hora
    )::TEXT;

    PERFORM pg_notify('lembrete_channel', payload);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--

CREATE TRIGGER lembrete_insert_trigger
AFTER INSERT ON lembretes
FOR EACH ROW
EXECUTE FUNCTION notify_lembrete_insert();

--

--teste
INSERT INTO lembretes (titulo, descricao, data_hora)
VALUES ('Comprar leite', 'Ir ao mercado comprar leite', '2025-03-09 10:00:00');