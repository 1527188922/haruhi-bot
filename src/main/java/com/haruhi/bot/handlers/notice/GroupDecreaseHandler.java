package com.haruhi.bot.handlers.notice;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.notice.IGroupDecreaseEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Slf4j
@Component
public class GroupDecreaseHandler implements IGroupDecreaseEvent {


    @Override
    public void onGroupDecrease(final Message message) {
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            Client.sendMessage(null,message.getGroupId(), MessageEventEnum.group, MessageFormat.format("{0} 离开了本群。",message.getUserId()), GocqActionEnum.SEND_MSG,true);
        });
    }
}
