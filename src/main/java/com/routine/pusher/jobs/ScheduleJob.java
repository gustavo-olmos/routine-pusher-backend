package com.routine.pusher.jobs;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.implementation.NotificadorServiceImpl;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ScheduleJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleJob.class);

    @Override
    public void execute( JobExecutionContext context ) throws JobExecutionException
    {
        JobDataMap map = context.getJobDetail().getJobDataMap();

        LembreteDTO dto = (LembreteDTO) map.get(NotificadorServiceImpl.class.getSimpleName());

        LOGGER.info("Remaining fire count is '{}'", dto.getIntervalo());
    }
}
