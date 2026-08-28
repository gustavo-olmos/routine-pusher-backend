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
     * Inatividade a partir da qual a sessão vira candidata à faxina. O valor é curto de propósito:
     * a sessão é também a trava de custo — lembretes de visitante que foi embora não podem ocupar
     * banco e agendador para sempre.
     */
    public static final Duration JANELA_INATIVIDADE = Duration.ofMinutes( 30 );

    /**
     * Teto de lembretes simultâneos por sessão. É a defesa contra o visitante que resolve testar os
     * limites do motor: o estrago individual fica pequeno, e o coletivo vira soma de estragos
     * pequenos em vez de produto.
     */
    public static final int LIMITE_LEMBRETES = 10;

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
