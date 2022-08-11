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

    public static Map<String,String> cache = new ConcurrentHashMap<>(10);

    @Override
    public int weight() {
        return 95;
    }
    private String answer;
    public boolean matching(final Message message, final String command) {
        String answer = cache.get(message.getGroup_id() + "-" + command);
        if(answer != null){
            this.answer = answer;
            return true;
        }
        this.answer = null;
        return false;
    }

    @Override
    public boolean onGroup(Message message, String command) {
        if(!matching(message,command)){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group,this.answer, GocqActionEnum.SEND_MSG,false);
        });
        return true;
    }
}
