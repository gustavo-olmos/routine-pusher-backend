package com.routine.pusher.config;

import org.quartz.spi.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean( JobFactory jobFactory )
    {
        SchedulerFactoryBean factory = new SchedulerFactoryBean( );
        factory.setJobFactory( jobFactory );
        factory.setApplicationContextSchedulerContextKey( "applicationContext" );

        return factory;
    }
}
