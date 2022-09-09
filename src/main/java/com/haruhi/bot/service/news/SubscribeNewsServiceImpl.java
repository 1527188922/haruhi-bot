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
import org.apache.commons.lang3.StringUtils;
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
            if (StringUtils.isNotBlank(responseStr)) {
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
    public void sendGroup(List<NewsBy163Resp> list,List<String> groupIds) {
        List<ForwardMsg> newsGroupMessage = createNewsGroupMessage(list);
        for (String groupId : groupIds) {
            Client.sendRestMessage(null,groupId, MessageEventEnum.group,"今日新闻",GocqActionEnum.SEND_MSG,true);
            Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,groupId,newsGroupMessage);
        }
    }

    private List<ForwardMsg> createNewsGroupMessage(List<NewsBy163Resp> list){
        List<ForwardMsg> forwardMsgs = new ArrayList<>(list.size());
        KQCodeUtils instance = KQCodeUtils.getInstance();
        for (NewsBy163Resp e : list) {
            StringBuilder stringBuilder = new StringBuilder("【");
            stringBuilder.append(e.getTitle()).append("】\n[");
            stringBuilder.append(DateTimeUtil.dateTimeFormat(e.getLmodify(),DateTimeUtil.FormatEnum.yyyyMMddHHmmss)).append("]\n");
            stringBuilder.append(e.getDigest()).append("\n");
            if(StringUtils.isNotBlank(e.getImgsrc())){
                String cq = instance.toCq(CqCodeTypeEnum.image.getType(), "file=" + e.getImgsrc());
                stringBuilder.append(cq).append("\n");
            }
            if(StringUtils.isNotBlank(e.getUrl())){
                stringBuilder.append("详情:").append(e.getUrl());
            }
            forwardMsgs.add(CommonUtil.createForwardMsgItem(stringBuilder.toString(),BotConfig.SELF_ID, BotConfig.NAME));
        }
        return forwardMsgs;
    }

    @Override
    public void sendPrivate(List<NewsBy163Resp> list,List<String> userIds) {

    }
}
