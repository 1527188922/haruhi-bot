package com.haruhi.bot.handlers;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.Answer;
import com.haruhi.bot.dto.gocq.response.AnswerBox;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.handlers.event.IOnGroupMessageEvent;
import com.haruhi.bot.service.checkin.CheckinService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class CheckinHandler implements IOnGroupMessageEvent {

    @Autowired
    private CheckinService checkinService;

    @Override
    public boolean matches(Message message,String command, AtomicInteger total) {
        return command.matches(RegexEnum.CHECKIN.getValue());
    }

    @Override
    public int weight() {
        return 99;
    }

    @Override
    public void onGroup(Message message, String command) {

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            try {
                AnswerBox<Answer> answerBox = new AnswerBox<>();
                Answer answer = new Answer();
                answer.setAuto_escape(true);
                answer.setGroup_id(message.getGroup_id());
                answer.setMessage_type(message.getMessage_type());
                answer.setUser_id(message.getUser_id());
                checkinService.checkin(answer,message);
                answerBox.setParams(answer);
                answerBox.setAction(GocqActionEnum.SEND_MSG.getAction());
                Client.sendMessage(answerBox);
            }catch (Exception e){
                log.error("处理命令:[{}]时异常:{}",command,e);
            }
        });
    }
}
