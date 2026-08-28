package com.routine.pusher.infrastructure.config;

import com.routine.pusher.application.job.SessaoExpiradaJob;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.infrastructure.web.SessaoAnonimaFilter;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Liga as duas pontas do ciclo de vida da sessão anônima: o filtro que a cria/renova a cada
 * requisição de API, e a faxina recorrente que remove as expiradas.
 */
@Configuration
public class SessaoConfig
{
    private static final String GRUPO_MANUTENCAO = "manutencao";
    private static final String JOB_FAXINA = "faxina-sessao-expirada";

    /** A cada 5 minutos: com janela de inatividade de 30, o atraso máximo da coleta é ruído. */
    private static final String CRON_FAXINA = "0 */5 * * * ?";

    /**
     * Registrado aqui, e não como {@code @Component}, por duas razões: restringir o filtro a
     * {@code /api/*} (páginas e Swagger não ganham sessão) e não deixá-lo vazar para fatias de
     * teste {@code @WebMvcTest}, que instanciam qualquer Filter que seja bean de componente.
     */
    @Bean
    public FilterRegistrationBean<SessaoAnonimaFilter> sessaoAnonimaFilter( SessaoAnonimaRepository repository )
    {
        FilterRegistrationBean<SessaoAnonimaFilter> registro =
                new FilterRegistrationBean<>( new SessaoAnonimaFilter( repository ) );
        registro.addUrlPatterns( "/api/*" );

        return registro;
    }

    /**
     * Agendamento idempotente: com job store JDBC o agendamento sobrevive ao restart, então o boot
     * não pode simplesmente agendar de novo — o {@code replace} substitui o que já existe em vez de
     * falhar ou duplicar.
     */
    @Bean
    public ApplicationRunner agendarFaxinaDeSessao( Scheduler scheduler )
    {
        return args -> {
            JobDetail job = JobBuilder.newJob( SessaoExpiradaJob.class )
                    .withIdentity( JOB_FAXINA, GRUPO_MANUTENCAO )
                    .storeDurably( )
                    .build( );

            Trigger trigger = TriggerBuilder.newTrigger( )
                    .withIdentity( JOB_FAXINA, GRUPO_MANUTENCAO )
                    .forJob( job )
                    .withSchedule( CronScheduleBuilder.cronSchedule( CRON_FAXINA )
                            // Faxina atrasada não se acumula: rodar uma vez agora paga a dívida
                            // inteira, então misfires viram uma execução só.
                            .withMisfireHandlingInstructionDoNothing( ) )
                    .build( );

            scheduler.scheduleJob( job, Set.of( trigger ), true );
        };
    }
}
