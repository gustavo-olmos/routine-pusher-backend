package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.strategy.NotificacaoCaseStrategy;
import com.routine.pusher.core.domain.notificacao.factory.NotificacaoStrategyFactory;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao
{
    private static final int FATOR_CANDIDATOS = 4;

    private Long id;
    private List<String> metodo;
    private LocalTime horario;
    private LocalDateTime proximaExecucao;
    private LocalDateTime ultimaExecucao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<LocalDateTime> datasEspecificadas;


    public boolean aindaTemNotificacao( Lembrete lembrete )
    {
        return this.aindaTemNotificacao( lembrete, null );
    }

    public boolean aindaTemNotificacao( Lembrete lembrete, FeriadoPort feriados )
    {
        return Objects.nonNull( this.calcularProximaNotificacao( lembrete, feriados ) );
    }

    /**
     * Com política de dia útil o próximo disparo deixa de ser "o que a recorrência calculou" e passa
     * a ser "o primeiro que o calendário aceita" — que é exatamente o primeiro item da projeção.
     * Reaproveitar a projeção aqui é o que impede o agendamento de divergir da prévia exibida.
     * <p>
     * Sem política, o caminho original segue intacto: ele é o único que ainda tem efeito colateral
     * ({@code DatasEspecificadasStrategy} grava {@code dataFim}) e mexer nele mudaria a saída.
     */
    public LocalDateTime calcularProximaNotificacao( Lembrete lembrete, FeriadoPort feriados )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );

        if( feriados == null || recorrencia == null || !recorrencia.exigeDiaUtil( ) )
            return this.calcularProximaNotificacao( lembrete );

        return this.calcularProximasNotificacoes( lembrete, 1, feriados ).stream( )
                   .findFirst( )
                   .orElse( null );
    }

    /**
     * Sem strategy não há quando: o lembrete não trouxe recorrência, datas específicas nem intervalo
     * válido. Devolver nulo é o mesmo contrato de "não há próxima execução" que a projeção já usava
     * — antes deste guard, a factory devolvia nulo e o agendamento estourava NullPointerException.
     */
    public LocalDateTime calcularProximaNotificacao( Lembrete lembrete )
    {
        NotificacaoCaseStrategy<Lembrete> caseStrategy = NotificacaoStrategyFactory.getStrategy( lembrete );

        return caseStrategy == null ? null : caseStrategy.calcularProximaNotificacao( lembrete );
    }

    /**
     * Projeta as próximas execuções para leitura, sem alterar o lembrete. A recorrência responde
     * "quando dispara"; os limites de negócio são aplicados aqui, porque não pertencem a nenhuma
     * strategy isolada: a validade encerra a série numa data e a cota a encerra numa contagem.
     * <p>
     * É uma prévia, não uma promessa — o agendamento real continua sendo um disparo de cada vez, e
     * uma conclusão ou alteração do lembrete refaz a série.
     */
    public List<LocalDateTime> calcularProximasNotificacoes( Lembrete lembrete, int limite )
    {
        return this.calcularProximasNotificacoes( lembrete, limite, null );
    }

    public List<LocalDateTime> calcularProximasNotificacoes( Lembrete lembrete, int limite, FeriadoPort feriados )
    {
        NotificacaoCaseStrategy<Lembrete> caseStrategy = NotificacaoStrategyFactory.getStrategy( lembrete );
        if( caseStrategy == null || limite <= 0 ) return List.of( );

        Recorrencia recorrencia = lembrete.getRecorrencia( );
        boolean comPolitica = feriados != null && recorrencia != null && recorrencia.exigeDiaUtil( );

        List<LocalDateTime> ocorrencias =
                caseStrategy.calcularProximasNotificacoes( lembrete, candidatos( limite, comPolitica ) );

        if( comPolitica )
            ocorrencias = recorrencia.getPoliticaDiaUtil( ).aplicar( ocorrencias, feriados );

        return ocorrencias.stream( )
                .filter( data -> dataFim == null || !data.isAfter( dataFim ) )
                .limit( cotaRestante( lembrete, limite ) )
                .toList( );
    }

    /**
     * Com política, parte das ocorrências geradas será descartada ou deslocada, então é preciso
     * gerar com folga. O fator é fixo de propósito: cobre o pior caso real — Carnaval encosta quatro
     * dias não úteis seguidos — sem transformar a projeção num laço cujo tamanho o usuário escolhe.
     * Quem garante que essa folga basta é a recusa de política em recorrência abaixo de um dia
     * ({@code Recorrencia.politicaDiaUtilEhCompativel}).
     */
    private int candidatos( int limite, boolean comPolitica )
    {
        return comPolitica ? limite * FATOR_CANDIDATOS : limite;
    }

    private long cotaRestante( Lembrete lembrete, int limite )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        if( recorrencia == null || recorrencia.getQuantidade( ) == null ) return limite;

        return Math.min( limite, Math.max( 0, recorrencia.getQuantidade( ) ) );
    }
}
