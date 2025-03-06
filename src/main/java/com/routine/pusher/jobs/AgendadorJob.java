package com.routine.pusher.jobs;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.implementation.AgendadorServiceImpl;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class AgendadorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorJob.class);

    @Override
    public void execute( JobExecutionContext context ) throws JobExecutionException
    {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        LembreteDTO dto = (LembreteDTO) map.get(AgendadorServiceImpl.class.getSimpleName());

        LOGGER.info("Intervalo de notificação {}", dto.getIntervalo());
    }
}
