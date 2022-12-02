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
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 处理戳一戳
 */
@Slf4j
@Component
public class PokeHandler implements IPokeEvent {

    public volatile static List<String> cache = new CopyOnWriteArrayList<>();

    @Override
    public void onPoke(final Message message) {
        if(!message.getSelfId().equals(message.getTargetId()) || message.getSelfId().equals(message.getUserId()) || cache.size() == 0){
            return;
        }

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                int size = cache.size();
                if(size > 0){
                    String reply = cache.get(CommonUtil.randomInt(0, size - 1));
                    if(MessageEventEnum.group.getType().equals(message.getMessageType())){
                        if("".equals(reply)){
                            KQCodeUtils instance = KQCodeUtils.getInstance();
                            String s = instance.toCq(CqCodeTypeEnum.poke.getType(), "qq=" + message.getUserId());
                            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,s, GocqActionEnum.SEND_MSG,false);
                        }else{
                            Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.group,reply, GocqActionEnum.SEND_MSG,true);
                        }
                    }else if(MessageEventEnum.privat.getType().equals(message.getMessageType())){
                        // gocq私聊不能发送给戳一戳 所以这里只回复文字
                        while (Strings.isBlank(reply)){
                            reply = cache.get(CommonUtil.randomInt(0, size - 1));
                        }
                        Client.sendMessage(message.getUserId(),message.getGroupId(), MessageEventEnum.privat,reply, GocqActionEnum.SEND_MSG,true);
                    }
                }
            }catch (Exception e){
                log.error("处理戳一戳发生异常",e);
            }
        });
    }
}
