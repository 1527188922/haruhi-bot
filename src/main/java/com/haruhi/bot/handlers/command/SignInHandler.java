package com.haruhi.bot.handlers.command;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.dto.response.AnswerBox;
import com.haruhi.bot.dto.request.Message;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SignInHandler extends AbstractCommandHandler {
    private JSONObject json;
    public SignInHandler(){
        log.info("签到处理类初始化...");
    }
    public SignInHandler(JSONObject json){
        this.json = json;
    }
    @Override
    public RegexEnum getRegex() {
        return RegexEnum.SIGNIN;
    }

    @Override
    public SignInHandler getSubclass(JSONObject json){
        return new SignInHandler(json);
    }

    @Override
    protected boolean customMatches(JSONObject json,String command) {
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
            answer.setMessage("签到成功");
            answerBox.setParams(answer);
            answerBox.setAction(GocqActionEnum.SEND_MSG.getAction());
            Client.sendMessage(JSONObject.toJSONString(answerBox));
        }catch (Exception e){
            log.error("处理命令:[{}]时异常:{}",getRegex().getValue(),e);
        }
    }
}
