package com.haruhi.bot.handlers.notice;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.notice.IPokeEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 处理戳一戳
 */
@Slf4j
@Component
public class PokeHandler implements IPokeEvent {

    public volatile static List<String> cache = new ArrayList<>();

    @Override
    public boolean onPoke(final Message message) {
        if(message.getSelf_id().equals(message.getUser_id()) || cache.size() == 0){
            return false;
        }

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            int size = cache.size();
            if(size > 0){
                String reply = cache.get(CommonUtil.randomInt(0, size - 1));
                if(MessageEventEnum.group.getType().equals(message.getMessage_type())){
                    if("".equals(reply)){
                        KQCodeUtils instance = KQCodeUtils.getInstance();
                        String s = instance.toCq(CqCodeTypeEnum.poke.getType(), "qq=" + message.getUser_id());
                        Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group,s, GocqActionEnum.SEND_MSG,false);
                    }else{
                        Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group,reply, GocqActionEnum.SEND_MSG,true);
                    }
                }else if(MessageEventEnum.privat.getType().equals(message.getMessage_type())){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.privat,reply, GocqActionEnum.SEND_MSG,true);
                }
            }

        });
        return true;
    }
}
