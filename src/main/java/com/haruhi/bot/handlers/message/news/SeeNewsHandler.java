package com.haruhi.bot.handlers.message.news;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.news.response.NewsBy163Resp;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.news.SubscribeNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SeeNewsHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 81;
    }

    @Override
    public String funName() {
        return "查看每日新闻";
    }
    @Autowired
    private SubscribeNewsService subscribeNewsService;

    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!command.matches(RegexEnum.SEE_TODAY_NEWS.getValue())){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                List<NewsBy163Resp> newsBy163Resps = subscribeNewsService.requestNewsBy163();
                if (MessageEventEnum.group.getType().equals(message.getMessage_type())) {
                    subscribeNewsService.sendGroup(newsBy163Resps,message.getGroup_id());
                }else if (MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                    subscribeNewsService.sendPrivate(newsBy163Resps,message.getUser_id());
                }
            }catch (Exception e){
                log.error("查看今日新闻异常",e);
            }
        });
        return true;
    }
}
