package com.haruhi.bot.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.job.schedule.AbstractJob;
import com.haruhi.bot.service.pixiv.PixivService;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
//@Component
public class DownloadPixivJob extends AbstractJob {

//    @Value("${cron.downloadPixivJob}")
    private String cron;

    @Override
    public String cronExpression() {
        return cron;
    }

    @Autowired
    private PixivService pixivService;

    private static Map<String,Object> param;
    private static Map<String,Object> paramR18;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(param == null){
            param = new HashMap<>();
            param.put("num",20);
            param.put("r18",0);
        }
        for (int i = 0; i < 2; i++) {
            ThreadPoolFactory.getDownloadThreadPool().execute(new DownloadPixivJob.downloadTask(pixivService,param));
        }
        if(paramR18 == null){
            paramR18 = new HashMap<>();
            paramR18.put("num",20);
            paramR18.put("r18",1);
        }
        ThreadPoolFactory.getDownloadThreadPool().execute(new DownloadPixivJob.downloadTask(pixivService,paramR18));
    }
    private static String url = "https://api.lolicon.app/setu/v2";
    public static class downloadTask implements Runnable{
        private Map<String,Object> param;
        private PixivService pixivService;

        downloadTask(PixivService pixivService,Map<String,Object> param){
            this.pixivService = pixivService;
            this.param = param;
        }

        @Override
        public void run() {
            try {
                Response response = RestUtil.sendGetRequest(RestUtil.getRestTemplate(11 * 1000), url, param, Response.class);
                if(response == null){
                    Client.sendMessage(BotConfig.SUPER_USER,null, MessageEventEnum.privat, MessageFormat.format("no data\n本次请求为null\n线程：{0}",Thread.currentThread().getName()), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                List<PixivItem> data = response.getData();
                if(data == null || data.size() == 0){
                    Client.sendMessage(BotConfig.SUPER_USER,null, MessageEventEnum.privat, MessageFormat.format("no data\n本次请求data为null或者size为0\n线程：{0}",Thread.currentThread().getName()), GocqActionEnum.SEND_MSG,true);
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
                Client.sendMessage(BotConfig.SUPER_USER,null, MessageEventEnum.privat, MessageFormat.format("success\n本次添加{0}条\n线程：{1}",total,Thread.currentThread().getName()), GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                Client.sendMessage(BotConfig.SUPER_USER,null, MessageEventEnum.privat, MessageFormat.format("exception\n下载发生异常\n线程：{0}\n异常：{1}",Thread.currentThread().getName(),e.getMessage()), GocqActionEnum.SEND_MSG,true);
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
