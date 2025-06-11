package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class Lembrete
{
    private Long id;
    private LocalDateTime dataCriacao;

    private String titulo;
    private String descricao;
    private String status;
    private Categoria categoria;
    private List<String> metodoNotificacao;
    private Recorrencia recorrencia;

    private LocalTime horario;
    private LocalDateTime proxNotificacao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<LocalDateTime> datasEspecificadas;

    public Lembrete( )
    {
        this.dataCriacao = LocalDateTime.now( );
        this.status = EnumStatusConclusao.PENDENTE.name( );
        calcularProximaNotificacao();
    }


    public void agendarLembrete( )
    {
        AgendadorJob.agendar( this );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }

    public LocalDateTime calcularProximaNotificacao( )
    {
        if( !datasEspecificadas.isEmpty( ) )
            proxNotificacao = datasEspecificadas.get( 0 );

        Duration intervalo = recorrencia.montarIntevalo( );
        if( intervalo.isPositive( ) )
            proxNotificacao = dataInicio.plus( intervalo );
        // ainda precisa validar com campo ultimaExecucao;

        //TODO: implementar os casos em strategies

        return proxNotificacao;
    }

    public String montarCronExpression( )
    {
        return String.format("%d %d %d", horario.getSecond( ), horario.getMinute( ), horario.getHour( ) )
                     .concat( recorrencia.montarCronExpression( ) );
    }

    // 3. Se horario estiver preenchido:
    //Use em conjunto com: diaDaSemana, diasFixosNoMes ou posicaoDaSemanaNoMes

    //3.1. Se diaDaSemana está preenchido (recorrência semanal):
    // Para cada dia da semana especificado:
    //    - Verifique o próximo dia daquela semana em relação a hoje
    //    - Combine com o horario
    // Retorne o menor resultado (mais próximo)

    // 3.2. Se diasFixosNoMes está preenchido (recorrência mensal, por dia fixo):
    // Para cada dia fixo no mês (ex: 1, 15, 28):
    //    - Combine com horario
    //    - Verifique se é futuro (hoje ou depois)
    //    - Se passou neste mês, vá para o próximo
    // Retorne a menor data

    // 3.3. Se posicaoDaSemanaNoMes e diaDaSemana estão preenchidos (ex: 2ª terça-feira do mês):
    // Ex: posicao=2, diaDaSemana="TUESDAY"
    // Calcular a 2ª terça-feira do mês atual ou do próximo
    // Combine com horario
    // Retorne se for futura, senão repita para o mês seguinte

    //Fluxo de decisão consolidado
    //1. Se datasEspecificadas != vazio → Retornar a mais próxima no futuro

    //2. Se intervalo != null → Baseado na última execução ou data de criação + intervalo → calcular próxima

    //3. Se horario != null:
    //     a. Se diaDaSemana != vazio:
    //         → Recorrência semanal
    //     b. Se diasFixosNoMes != vazio:
    //         → Recorrência mensal por dia fixo
    //     c. Se posicaoDaSemanaNoMes != null && diaDaSemana != vazio:
    //         → Recorrência mensal por posição da semana
}
