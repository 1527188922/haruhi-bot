package com.haruhi.bot.service.news;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.ThirdPartyURL;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.news.response.NewsBy163Resp;
import com.haruhi.bot.entity.SubscribeNews;
import com.haruhi.bot.mapper.SubscribeNewsMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.DateTimeUtil;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubscribeNewsServiceImpl extends ServiceImpl<SubscribeNewsMapper, SubscribeNews> implements SubscribeNewsService{

    @Override
    public List<NewsBy163Resp> requestNewsBy163(){
        log.info("开始获取网易新闻...");
        long l = System.currentTimeMillis();
        String sourceId = "T1348647853363";
        try {
            String responseStr = RestUtil.sendGetRequest(RestUtil.getRestTemplate(2 * 1000), ThirdPartyURL.NEWS_163, null, String.class);
            if (Strings.isNotBlank(responseStr)) {
                JSONObject responseJson = JSONObject.parseObject(responseStr);
                JSONArray jsonArray = responseJson.getJSONArray(sourceId);
                if(!CollectionUtils.isEmpty(jsonArray)){
                    List<NewsBy163Resp> newsBy163Resps = JSONArray.parseArray(jsonArray.toJSONString(), NewsBy163Resp.class);
                    newsBy163Resps = newsBy163Resps.stream().collect(
                            Collectors.collectingAndThen(Collectors.toCollection(()-> new TreeSet<>(Comparator.comparing(NewsBy163Resp::getPostid))), ArrayList::new)
                    ).stream().sorted(Comparator.comparing(NewsBy163Resp::getLmodify).reversed()).collect(Collectors.toList());
                    log.info("获取网易新闻完成,耗时:{}",System.currentTimeMillis() - l);
                    return newsBy163Resps;
                }
            }
            return null;
        }catch (Exception e){
            log.error("获取网易新闻异常",e);
            return null;
        }
    }

    @Override
    public void sendGroup(List<NewsBy163Resp> list,String... groupIds) {
        if(groupIds != null){
            List<ForwardMsg> newsGroupMessage = createNewsGroupMessage(list);
            for (String groupId : groupIds) {
                Client.sendRestMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,groupId,newsGroupMessage);
            }
        }
    }

    private List<ForwardMsg> createNewsGroupMessage(List<NewsBy163Resp> list){
        List<ForwardMsg> forwardMsgs = new ArrayList<>(list.size() + 1);
        KQCodeUtils instance = KQCodeUtils.getInstance();
        forwardMsgs.add(CommonUtil.createForwardMsgItem("今日新闻",BotConfig.SELF_ID, BotConfig.NAME));
        for (NewsBy163Resp e : list) {
            String newsItemMessage = createNewsItemMessage(e, instance);
            forwardMsgs.add(CommonUtil.createForwardMsgItem(newsItemMessage,BotConfig.SELF_ID, BotConfig.NAME));
        }
        return forwardMsgs;
    }
    private String createNewsItemMessage(NewsBy163Resp e,KQCodeUtils instance){
        StringBuilder stringBuilder = new StringBuilder("【");
        stringBuilder.append(e.getTitle()).append("】\n[");
        stringBuilder.append(DateTimeUtil.dateTimeFormat(e.getLmodify(), DateTimeUtil.PatternEnum.yyyyMMddHHmmss)).append("]\n");
        stringBuilder.append(e.getDigest()).append("\n");
        if(Strings.isNotBlank(e.getImgsrc())){
            String cq = instance.toCq(CqCodeTypeEnum.image.getType(), "file=" + e.getImgsrc());
            stringBuilder.append(cq).append("\n");
        }
        if(Strings.isNotBlank(e.getUrl())){
            stringBuilder.append("详情:").append(e.getUrl());
        }else{
            boolean hasUrl = false;
            if(Strings.isNotBlank(e.getPostid())){
                hasUrl = true;
                e.setUrl("https://3g.163.com/dy/article/" + e.getPostid() + ".html");
            }else if(Strings.isNotBlank(e.getDocid())){
                hasUrl = true;
                e.setUrl("https://3g.163.com/dy/article/" + e.getDocid() + ".html");
            }
            if(hasUrl){
                stringBuilder.append("详情:").append(e.getUrl());
            }
        }
        return stringBuilder.toString();
    }
    private String createNewsPrivateMessage(List<NewsBy163Resp> list){
        StringBuilder stringBuilder = new StringBuilder();
        KQCodeUtils instance = KQCodeUtils.getInstance();
        for (NewsBy163Resp e : list) {
            stringBuilder.append(createNewsItemMessage(e,instance)).append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public void sendPrivate(List<NewsBy163Resp> list,String... userIds) {
        if(userIds != null){
            List<List<NewsBy163Resp>> lists = CommonUtil.averageAssignList(list, 10);
            for (String userId : userIds) {
                for (List<NewsBy163Resp> newsBy163Resps : lists) {
                    Client.sendMessage(userId,null,MessageEventEnum.privat,createNewsPrivateMessage(newsBy163Resps),GocqActionEnum.SEND_MSG,false);
                }
            }
        }

    }
}
