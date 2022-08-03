package com.haruhi.bot.job;

import com.haruhi.bot.job.schedule.AbstractJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DownloadPixivJob extends AbstractJob {
    @Override
    public String cronExpression() {
        return "* * * * * ?";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("job running...");
    }
}
