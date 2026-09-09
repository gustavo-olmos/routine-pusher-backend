package com.routine.pusher.core.domain.sessao;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * O "usuário" do demo. Quem chega por um post não cadastra login e senha para testar um site que
 * talvez nunca mais abra — então a identidade nasce sozinha no primeiro request, viaja num cookie e
 * expira por inatividade. Tudo que o visitante cria pertence à sessão, e morre com ela.
 */
@Data
public class SessaoAnonima
{
    /**
     * Inatividade a partir da qual a sessão vira candidata à faxina — e, com ela, todos os lembretes
     * que o visitante criou.
     * <p>
     * Era de 30 minutos, valor herdado de quando o demo só precisava sobreviver a "criar um lembrete
     * e vê-lo disparar na hora". Isso quebrava o produto no caso normal: quem pedia um lembrete para
     * a semana seguinte, fechava a aba e voltava depois não encontrava nada — a faxina já havia
     * apagado a sessão e o agendamento junto, e o lembrete nunca disparava.
     * <p>
     * Quarenta e oito horas é o meio-termo escolhido: cobre "amanhã de manhã" e o visitante que volta
     * no dia seguinte, sem manter agendador e banco ocupados por semanas com quem passou uma vez.
     * <p>
     * A contrapartida é conhecida e aceita: lembrete marcado para daqui a duas semanas não sobrevive
     * se o visitante não voltar dentro de 48h. Num demo, esse caso importa menos que o custo de
     * retenção longa — e quem quiser retenção de verdade precisa de conta, não de sessão anônima.
     */
    public static final Duration JANELA_INATIVIDADE = Duration.ofHours( 48 );

    /**
     * Teto de lembretes simultâneos por sessão. É a defesa contra o visitante que resolve testar os
     * limites do motor: o estrago individual fica pequeno, e o coletivo vira soma de estragos
     * pequenos em vez de produto.
     */
    public static final int LIMITE_LEMBRETES = 10;

    /**
     * Teto de chamadas de IA por sessão. Diferente do limite de lembretes (que protege memória e
     * agendador), este protege o bolso: cada chamada ao modelo tem custo em dinheiro, e com o teto
     * o pior caso vira aritmética — sessões x cota x custo por chamada.
     */
    public static final int LIMITE_CHAMADAS_IA = 10;

    /** Chave interna do banco — mesmo papel (e mesmas razões) do id do lembrete. */
    private Long id;

    /** Identidade pública: é este valor que viaja no cookie e escopa as consultas. */
    private UUID uuid;

    private LocalDateTime dataCriacao;
    private LocalDateTime ultimoAcesso;

    public SessaoAnonima( )
    {
        this.uuid = UUID.randomUUID( );
        this.dataCriacao = LocalDateTime.now( );
        this.ultimoAcesso = this.dataCriacao;
    }

    public boolean expirada( LocalDateTime agora )
    {
        return ultimoAcesso == null || ultimoAcesso.plus( JANELA_INATIVIDADE ).isBefore( agora );
    }

    public LocalDateTime expiraEm( )
    {
        return ultimoAcesso == null ? null : ultimoAcesso.plus( JANELA_INATIVIDADE );
    }
}
