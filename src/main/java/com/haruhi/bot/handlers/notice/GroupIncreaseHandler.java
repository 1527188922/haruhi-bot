package com.haruhi.bot.handlers.notice;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.notice.IGroupIncreaseEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Slf4j
@Component
public class GroupIncreaseHandler implements IGroupIncreaseEvent {


    @Override
    public void onGroupIncrease(final Message message) {
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{

            KQCodeUtils instance = KQCodeUtils.getInstance();
            String at = instance.toCq(CqCodeTypeEnum.at.getType(), "qq=" + message.getUser_id());
            String faces = "";
            String face = instance.toCq(CqCodeTypeEnum.face.getType(), "id=" + 144);
            for (int i = 0; i < 3; i++) {
                faces += face;
            }
            Client.sendMessage(null,message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("{0} 欢迎新人~{1}",at,faces), GocqActionEnum.SEND_MSG,false);
        });
    }
}
