package com.haruhi.bot.handlers.command;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.request.Message;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.dto.response.AnswerBox;
import com.haruhi.bot.factory.ServiceFactory;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeeFavorabilityHandler extends AbstractCommandHandler{

    private JSONObject json;
    private String command;
    SeeFavorabilityHandler(){
        log.info("查看好感度处理类初始化...");
    }
    SeeFavorabilityHandler(JSONObject json){
        this.json = json;
    }
    @Override
    protected RegexEnum getRegex() {
        return null;
    }

    @Override
    protected AbstractCommandHandler getSubclass(JSONObject json) {
        return new SeeFavorabilityHandler(json);
    }

    @Override
    protected boolean customMatches(JSONObject json,String command) {
        if(command.matches(RegexEnum.SEE_FAVORABILITY.getValue())){
            if(MessageTypeEnum.group.getType().equals(json.getString("message_type"))){
                this.command = command;
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            AnswerBox<Answer> box = new AnswerBox<>();
            Answer answer = new Answer();
            Message message = JSONObject.parseObject(json.toJSONString(), Message.class);

            answer.setGroup_id(message.getGroup_id());
            answer.setMessage_type(message.getMessage_type());
            answer.setUser_id(message.getUser_id());
            ServiceFactory.checkinService.seeFavorability(answer,message);
            box.setAction(GocqActionEnum.SEND_MSG.getAction());
            box.setParams(answer);
            Client.sendMessage(box);
        }catch (Exception e){
            log.error("处理命令:[{}]时异常:{}",this.command,e);
        }


    }
}
