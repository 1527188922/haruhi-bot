package com.haruhi.bot.handlers;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.request.Message;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.dto.response.AnswerBox;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.handlers.event.IOnGroupMessageEvent;
import com.haruhi.bot.service.checkin.CheckinService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeeFavorabilityHandler implements IOnGroupMessageEvent {

    @Autowired
    private CheckinService checkinService;

    @Override
    public RegexEnum getRegex() {
        return RegexEnum.SEE_FAVORABILITY;
    }

    @Override
    public void onGroup(Message message, String command) {
        if(!command.matches(getRegex().getValue())){
            return;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                AnswerBox<Answer> box = new AnswerBox<>();
                Answer answer = new Answer();
                answer.setGroup_id(message.getGroup_id());
                answer.setMessage_type(message.getMessage_type());
                answer.setUser_id(message.getUser_id());
                checkinService.seeFavorability(answer,message);
                box.setAction(GocqActionEnum.SEND_MSG.getAction());
                box.setParams(answer);
                Client.sendMessage(box);
            }catch (Exception e){
                log.error("处理命令:[{}]时异常:{}",command,e);
            }
        });
    }
}
