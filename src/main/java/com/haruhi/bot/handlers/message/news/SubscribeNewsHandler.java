package com.haruhi.bot.handlers.message.news;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.news.SubscribeNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

            int type = 1;
            Integer count = 0;
            if (MessageEventEnum.group.getType().equals(message.getMessage_type())) {

            }else if (MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                type = 2;
            }
        });

        return true;
    }
}
