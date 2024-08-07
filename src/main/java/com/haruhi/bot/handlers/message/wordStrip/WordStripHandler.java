package com.haruhi.bot.handlers.message.wordStrip;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WordStripHandler implements IGroupMessageEvent {

    public static Map<String,String> cache = new ConcurrentHashMap<>();

    @Override
    public int weight() {
        return 95;
    }

    @Override
    public String funName() {
        return "词条监听";
    }

    @Override
    public boolean onGroup(final Message message,final String command) {
        String answer = cache.get(message.getGroupId() + "-" + command);
        if(answer == null){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group, answer, GocqActionEnum.SEND_MSG,false);
        });
        return true;
    }
}
