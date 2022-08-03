package com.haruhi.bot.job.schedule;

import org.quartz.Job;

public abstract class AbstractJob implements Job {
    public abstract String cronExpression();
}
