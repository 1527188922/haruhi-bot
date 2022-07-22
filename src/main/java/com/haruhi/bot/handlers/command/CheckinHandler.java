package com.haruhi.bot.handlers.command;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeConstant;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.dto.response.AnswerBox;
import com.haruhi.bot.dto.request.Message;
import com.haruhi.bot.factory.ServiceFactory;
import com.haruhi.bot.service.checkin.CheckinService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CheckinHandler extends AbstractCommandHandler {
    private JSONObject json;
    private String command;
    public CheckinHandler(){
        log.info("签到处理类初始化...");
    }
    public CheckinHandler(JSONObject json){
        this.json = json;
    }
    @Override
    public RegexEnum getRegex() {
        return null;
    }

    @Override
    public CheckinHandler getSubclass(JSONObject json){
        return new CheckinHandler(json);
    }

    @Override
    protected boolean customMatches(JSONObject json,String command) {
        if(command.matches(RegexEnum.SIGNIN.getValue())){
            if(MessageTypeConstant.group.equals(json.getString("message_type"))){
                this.command = command;
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            AnswerBox<Answer> answerBox = new AnswerBox<>();
            Answer answer = new Answer();
            Message message = JSONObject.parseObject(json.toJSONString(), Message.class);
            answer.setAuto_escape(true);
            answer.setGroup_id(message.getGroup_id());
            answer.setMessage_type(message.getMessage_type());
            answer.setUser_id(message.getUser_id());
            ServiceFactory.checkinService.checkin(answer);
            answerBox.setParams(answer);
            answerBox.setAction(GocqActionEnum.SEND_MSG.getAction());
            Client.sendMessage(JSONObject.toJSONString(answerBox));
        }catch (Exception e){
            log.error("处理命令:[{}]时异常:{}",getRegex() != null ? getRegex().getValue() : this.command,e);
        }
    }
}
