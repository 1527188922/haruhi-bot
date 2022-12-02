package com.haruhi.bot.handlers.message.news;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.SubscribeNews;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.news.SubscribeNewsService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnSubscribeNewsHandler implements IMessageEvent {


    @Override
    public int weight() {
        return 82;
    }

    @Override
    public String funName() {
        return "取消订阅新闻";
    }
    @Autowired
    private SubscribeNewsService subscribeNewsService;

    @Override
    public boolean onMessage(final Message message,final String command) {
        if (!command.matches(RegexEnum.UN_SUBSCRIBE_NEWS.getValue())) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                LambdaQueryWrapper<SubscribeNews> queryWrapper = new LambdaQueryWrapper<>();
                boolean isGroup = false;
                if (MessageEventEnum.group.getType().equals(message.getMessageType())) {
                    isGroup = true;
                    queryWrapper.eq(SubscribeNews::getGroupId,message.getGroupId()).eq(SubscribeNews::getType,1);
                }else if (MessageEventEnum.privat.getType().equals(message.getMessageType())){
                    queryWrapper.eq(SubscribeNews::getSubscriber,message.getUserId()).eq(SubscribeNews::getType,2);
                }
                int count = subscribeNewsService.count(queryWrapper);
                if(count == 0){
                    String tip = "";
                    if(isGroup){
                        tip = "本群还未订阅新闻";
                    }else{
                        tip = "你还未订阅新闻";
                    }
                    Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),tip, GocqActionEnum.SEND_MSG,true);
                    return;
                }

                subscribeNewsService.remove(queryWrapper);
                Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"取消订阅成功", GocqActionEnum.SEND_MSG,true);

            }catch (Exception e){
                log.error("取消订阅新闻异常",e);
            }
        });
        return true;
    }
}
