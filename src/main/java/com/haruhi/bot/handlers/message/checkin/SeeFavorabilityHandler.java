package com.haruhi.bot.handlers.message.checkin;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.Params;
import com.haruhi.bot.dto.gocq.request.RequestBox;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.service.checkin.CheckinService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SeeFavorabilityHandler implements IGroupMessageEvent {

    @Autowired
    private CheckinService checkinService;

    public boolean matching(final String command) {
        return command.matches(RegexEnum.SEE_FAVORABILITY.getValue());
    }

    @Override
    public int weight() {
        return 100;
    }

    @Override
    public String funName() {
        return "查看好感";
    }

    @Override
    public boolean onGroup(final Message message,final String command) {
        if(!matching(command)){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                RequestBox<Params> box = new RequestBox<>();
                Params answer = new Params();
                answer.setGroupId(message.getGroupId());
                answer.setMessageType(message.getMessageType());
                answer.setUserId(message.getUserId());
                checkinService.seeFavorability(answer,message);
                box.setAction(GocqActionEnum.SEND_MSG.getAction());
                box.setParams(answer);
                Client.sendMessage(box);
            }catch (Exception e){
                log.error("处理命令:[{}]时异常:{}",command,e);
            }
        });
        return true;
    }
}
