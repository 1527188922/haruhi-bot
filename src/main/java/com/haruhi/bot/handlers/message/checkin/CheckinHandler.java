package com.haruhi.bot.handlers.message.checkin;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.Answer;
import com.haruhi.bot.dto.gocq.request.AnswerBox;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.service.checkin.CheckinService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CheckinHandler implements IGroupMessageEvent {

    @Autowired
    private CheckinService checkinService;

    public boolean matching(final String command) {
        return command.matches(RegexEnum.CHECKIN.getValue());
    }

    @Override
    public int weight() {
        return 99;
    }

    @Override
    public String funName() {
        return "签到";
    }

    @Override
    public boolean onGroup(final Message message,final  String command) {
        if (!matching(command)) {
            return false;
        }
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
        return true;
    }
}
