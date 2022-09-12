package com.haruhi.bot.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.dto.news.response.NewsBy163Resp;
import com.haruhi.bot.entity.SubscribeNews;
import com.haruhi.bot.job.schedule.AbstractJob;
import com.haruhi.bot.service.news.SubscribeNewsService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "job.news.enable",havingValue = "1")
public class NewsJob extends AbstractJob {

    @Value("${job.news.cron}")
    private String cron;

    @Override
    public String cronExpression() {
        return cron;
    }

    @Autowired
    private SubscribeNewsService subscribeNewsService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (Client.connected()) {
            log.info("定时任务:发送每日新闻 开始执行");
            try {
                long l = System.currentTimeMillis();
                int count = subscribeNewsService.count(null);
                if(count > 0){
                    List<NewsBy163Resp> newsBy163Resps = subscribeNewsService.requestNewsBy163();
                    // 对群订阅的群进行发送
                    LambdaQueryWrapper<SubscribeNews> findByGroup = new LambdaQueryWrapper<>();
                    findByGroup.eq(SubscribeNews::getType,1);
                    List<SubscribeNews> listByGroup = subscribeNewsService.list(findByGroup);
                    if(!CollectionUtils.isEmpty(listByGroup)){
                        String[] strings = listByGroup.stream().map(SubscribeNews::getGroupId).toArray(String[]::new);
                        subscribeNewsService.sendGroup(newsBy163Resps,strings);
                    }

                    // 对私聊订阅的用户进行发送
                    LambdaQueryWrapper<SubscribeNews> findByPrivate = new LambdaQueryWrapper<>();
                    findByPrivate.eq(SubscribeNews::getType,2);
                    List<SubscribeNews> listByPrivate = subscribeNewsService.list(findByPrivate);
                    if(!CollectionUtils.isEmpty(listByPrivate)){
                        String[] strings = listByPrivate.stream().map(SubscribeNews::getSubscriber).toArray(String[]::new);
                        subscribeNewsService.sendPrivate(newsBy163Resps,strings);
                    }
                }
                log.info("定时任务:发送每日新闻 执行完成,耗时：{}",System.currentTimeMillis() - l);
            }catch (Exception e){
                log.error("定时任务:发送每日新闻异常",e);
            }
        }else{
            log.info("未连接gocq，本次定时任务不执行");
        }
    }
}
