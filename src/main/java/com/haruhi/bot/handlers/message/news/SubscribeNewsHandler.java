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

import java.util.Date;

@Slf4j
@Component
public class SubscribeNewsHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 83;
    }

    @Override
    public String funName() {
        return "订阅新闻";
    }
    @Autowired
    private SubscribeNewsService subscribeNewsService;

    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!command.matches(RegexEnum.SUBSCRIBE_NEWS.getValue())){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                LambdaQueryWrapper<SubscribeNews> queryWrapper = new LambdaQueryWrapper<>();
                boolean isGroup = false;
                if (MessageEventEnum.group.getType().equals(message.getMessage_type())) {
                    isGroup = true;
                    queryWrapper.eq(SubscribeNews::getGroupId,message.getGroup_id()).eq(SubscribeNews::getType,1);
                }else if (MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                    queryWrapper.eq(SubscribeNews::getSubscriber,message.getUser_id()).eq(SubscribeNews::getType,2);
                }
                int count = subscribeNewsService.count(queryWrapper);
                if(count > 0){
                    String tip = "";
                    if(isGroup){
                        tip = "本群已订阅新闻";
                    }else{
                        tip = "你已订阅新闻";
                    }
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),tip, GocqActionEnum.SEND_MSG,true);
                    return;
                }

                SubscribeNews param = new SubscribeNews();
                param.setSubscriber(message.getUser_id());
                param.setCreateTime(new Date());
                if(isGroup){
                    param.setGroupId(message.getGroup_id());
                    param.setType(1);
                }else{
                    param.setType(2);
                }
                subscribeNewsService.save(param);
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),"订阅成功", GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                log.error("订阅新闻异常",e);
            }
        });

        return true;
    }
}
