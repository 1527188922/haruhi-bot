package com.haruhi.bot.handlers;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.dto.aiChat.response.ChatResp;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.handlers.event.IOnMessageEvent;
import com.haruhi.bot.utils.RestUtil;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任何命令都没有匹配到
 * 并且群里at了机器人或者给机器人发私聊
 */
@Slf4j
@Component
public class AiChatHandler implements IOnMessageEvent {

    private static String url = "http://api.qingyunke.com/api.php";

    @Override
    public int weight() {
        return 1;
    }
    private String[] cqs;

    @Override
    public boolean matches(Message message,String command, AtomicInteger total) {
        if(total.get() == 0){
            if(MessageTypeEnum.privat.getType().equals(message.getMessage_type())){
                // 私聊了机器人
                this.cqs = null;
                return true;
            }
            if(MessageTypeEnum.group.getType().equals(message.getMessage_type())){
                KQCodeUtils utils = KQCodeUtils.getInstance();
                String[] cqs = utils.getCqs(command, CqCodeTypeEnum.at.getType());
                if(cqs == null || cqs.length == 0){
                    // 没有at机器人
                    this.cqs = null;
                    return false;
                }
                for (String cq : cqs) {
                    String qq = utils.getParam(cq, "qq", CqCodeTypeEnum.at.getType());
                    if(qq != null && qq.equals(message.getSelf_id())){
                        // 表示at了机器人
                        this.cqs = cqs;
                        return true;
                    }
                }
            }
        }
        this.cqs = null;
        return false;
    }

    @Override
    public void onMessage(Message message, String command) {
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
            String s = new String(command);
            if(this.cqs != null){
                for (String cq : this.cqs) {
                    s = s.replace(cq,"");
                }
            }

            HashMap<String, Object> urlParam = new HashMap<>();
            urlParam.put("key","free");
            urlParam.put("appid",0);
            urlParam.put("msg",s);
            ChatResp chatResp = RestUtil.sendGetRequest(RestUtil.getRestTemplate(5 * 1000), url, urlParam, ChatResp.class);
            if(chatResp != null){
                String content = chatResp.getContent();
                if(content != null){
                    String replace = content.replace("{br}", "\n");
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),replace,GocqActionEnum.SEND_MSG,true);
                }

            }
        });
    }

    private static String reg ="(?<=\\{face:)[0-9]*(?=\\})";
    private static String regex = ".*\\{face:.*\\}.*";
    private String f(String content){
        if(!content.matches(regex)){
            return content;
        }
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(content);
        List<String> matchStrs = new ArrayList<>();

        while (matcher.find()) {
            matchStrs.add(matcher.group());
        }

        for (int i = 0; i < matchStrs.size(); i++) {

            content.replace("{face:"+matchStrs.get(i)+"}","");
        }
        return content;
    }
}
