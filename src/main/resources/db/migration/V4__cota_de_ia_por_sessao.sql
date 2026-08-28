-- Cota de chamadas de IA por sessão anônima.
--
-- A trava de lembretes limita o que fica armazenado; esta limita o que se GASTA: cada chamada ao
-- modelo custa dinheiro de verdade, então o teto por sessão transforma o pior caso de "torcida"
-- em aritmética — sessões x cota x custo por chamada.
--
-- Contador na própria sessão (e não tabela de eventos) de propósito: a granularidade que importa
-- é "quanto esta sessão já gastou", e o consumo é um UPDATE atômico condicional (ver
-- SessaoAnonimaRepository.consumirChamadaIa), imune a corrida entre requisições paralelas.

ALTER TABLE sessao_anonima
    ADD COLUMN chamadas_ia INTEGER NOT NULL DEFAULT 0;
