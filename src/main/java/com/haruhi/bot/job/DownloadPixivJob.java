package com.haruhi.bot.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.ThirdPartyURL;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.job.schedule.AbstractJob;
import com.haruhi.bot.service.pixiv.PixivService;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(name = "job.downloadPixiv.enable",havingValue = "1")
public class DownloadPixivJob extends AbstractJob {

    @Value("${job.downloadPixiv.cron}")
    private String cron;
    @Value("${job.downloadPixiv.notice}")
    private String notice;

    @Override
    public String cronExpression() {
        return cron;
    }

    @Autowired
    private PixivService pixivService;

    private static Executor threadPool = null;
    public DownloadPixivJob(){
        if (threadPool == null) {
            threadPool = new ThreadPoolExecutor(4,8,60, TimeUnit.SECONDS,new ArrayBlockingQueue(30),
                    new CustomizableThreadFactory("pool-downloadPixivJob-"),new ThreadPoolExecutor.DiscardPolicy());
        }
    }

    private static Map<String,Object> param;
    private static Map<String,Object> paramR18;
    static {
        param = new HashMap<>(2);
        param.put("num",20);
        param.put("r18",0);

        paramR18 = new HashMap<>(2);
        paramR18.put("num",20);
        paramR18.put("r18",1);
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        for (int i = 0; i < 2; i++) {
            threadPool.execute(new DownloadTask(pixivService,param));
        }
        threadPool.execute(new DownloadTask(pixivService,paramR18));
    }

    private class DownloadTask implements Runnable{
        private Map<String,Object> param;
        private PixivService pixivService;

        DownloadTask(PixivService pixivService, Map<String,Object> param){
            this.pixivService = pixivService;
            this.param = param;
        }

        @Override
        public void run() {
            try {
                Response response = RestUtil.sendGetRequest(RestUtil.getRestTemplate(11 * 1000), ThirdPartyURL.LOLICON, param, Response.class);
                if(response == null){
                    log.warn("response = null");
                    return;
                }
                List<PixivItem> data = response.getData();
                if(CollectionUtils.isEmpty(data)){
                    log.warn("response.getData() = null");
                    return;
                }
                int total = 0;
                for (PixivItem e : data) {
                    LambdaQueryWrapper<Pixiv> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(Pixiv::getImgUrl,e.getUrls().getOriginal());
                    Pixiv one = pixivService.getOne(queryWrapper);
                    if(one == null){
                        Pixiv param = new Pixiv();
                        param.setPid(e.getPid());
                        param.setTitle(e.getTitle());
                        param.setWidth(e.getWidth());
                        param.setHeight(e.getHeight());
                        param.setImgUrl(e.getUrls().getOriginal());
                        param.setUid(e.getUid());
                        param.setAuthor(e.getAuthor());
                        param.setIsR18(e.getR18());
                        List<String> tags = e.getTags();
                        String str = "";
                        for (String tag : tags) {
                            str += tag + ",";
                        }
                        param.setTags(str.substring(0,str.length() - 1));
                        if(pixivService.save(param)){
                            total++;
                        }
                    }

                }
                if(total > 0 && "1".equals(notice)){
                    Client.sendMessage(BotConfig.SUPER_USER,null, MessageEventEnum.privat, 
                            MessageFormat.format("success\n本次添加{0}条\n线程：{1}",total,Thread.currentThread().getName()), GocqActionEnum.SEND_MSG,true);
                }
            }catch (Exception e){
                if("1".equals(notice) || "2".equals(notice)){
                    Client.sendMessage(BotConfig.SUPER_USER,null, MessageEventEnum.privat, 
                            MessageFormat.format("exception\n下载发生异常\n线程：{0}\n异常：{1}",Thread.currentThread().getName(),e.getMessage()), 
                            GocqActionEnum.SEND_MSG,true);    
                }
                log.error("下载pixiv异常",e);
            }
        }
    }

    @Data
    public static class Response implements Serializable {
        private String error;
        private List<PixivItem> data;
    }
    @Data
    public static class PixivItem implements Serializable {
        private String pid;
        private String p;
        private String uid;
        private String title;
        private String author;
        private Boolean r18;
        private Integer width;
        private Integer height;
        private List<String> tags;
        private String ext;
        private Long uploadDate;
        private Url urls;
    }
    @Data
    public static class Url implements Serializable {
        private String original;
    }
}
