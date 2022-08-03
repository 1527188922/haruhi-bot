package com.haruhi.bot.job.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class JobManage implements ApplicationContextAware {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void startAllJob(){
        log.info("开始启动定时任务...");
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        Map<String, AbstractJob> beansOfType = applicationContext.getBeansOfType(AbstractJob.class);
        for (AbstractJob value : beansOfType.values()) {

            String name = value.getClass().getSimpleName() + "_job";
            String trigger = value.getClass().getSimpleName() + "_trigger";
            String group = value.getClass().getSimpleName() + "_group";

            JobDetail detail = JobBuilder.newJob(value.getClass()).withIdentity(name, group).build();

            Trigger build = TriggerBuilder.newTrigger().withIdentity(trigger, group).withSchedule(CronScheduleBuilder.cronSchedule(value.cronExpression())).build();

            JobKey jobKey = new JobKey(name, group);

            TriggerKey triggerKey = new TriggerKey(trigger, group);
            try {
                if(scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)){
                    continue;
                }
            } catch (SchedulerException e) {
                continue;
            }

            try {
                scheduler.scheduleJob(detail,build);
                log.info("定时任务：{}启动成功",name);
            } catch (SchedulerException e) {
                log.error("定时任务启动异常{}",name);
                e.printStackTrace();
            }
        }
    }
}
