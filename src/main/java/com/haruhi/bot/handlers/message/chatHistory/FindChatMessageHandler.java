package com.haruhi.bot.handlers.message.chatHistory;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.TimeUnitEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.groupChatHistory.GroupChatHistoryService;
import com.haruhi.bot.ws.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FindChatMessageHandler implements IGroupMessageEvent {
    @Override
    public int weight() {
        return 91;
    }

    @Override
    public String funName() {
        return "群聊天记录搜索";
    }

    @Autowired
    private GroupChatHistoryService groupChatHistoryService;

    @Override
    public boolean onGroup(final Message message,final String command) {
        Param param = matching(message,command);
        if(param == null){
            return false;
        }

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new Task(groupChatHistoryService,message,param));
        return true;
    }

    private class Task implements Runnable{
        private GroupChatHistoryService service;
        private Message message;
        private FindChatMessageHandler.Param param;

        public Task(GroupChatHistoryService service,Message message,FindChatMessageHandler.Param param){
            this.service = service;
            this.message = message;
            this.param = param;
        }
        @Override
        public void run() {
            service.sendChatList(message,param);
        }
    }

    private Param matching(final Message message,final String command){
        for (Regex item : Regex.values()) {
            Pattern compile = Pattern.compile(item.value);
            Matcher matcher = compile.matcher(command);
            if(matcher.find()){
                Integer num = null;
                String args = null;
                try {
                    args = matcher.group(1);
                    num = Integer.valueOf(args);
                }catch (Exception e){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("错误的参数[{0}],请输入整数...",args),GocqActionEnum.SEND_MSG,true);
                    return null;
                }
                return new Param(num,item.timeUnit,item.messageType);
            }
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param {
        private Integer num;
        private TimeUnitEnum unit;
        private MessageType messageType;
    }

    public enum Regex{

        DAY_ALL("聊天记录(.*?)天",TimeUnitEnum.DAY,MessageType.ALL),
        HOUR_ALL("聊天记录(.*?)时",TimeUnitEnum.HOUR,MessageType.ALL),
        DAY_IMAGE("聊天图片(.*?)天",TimeUnitEnum.DAY,MessageType.IMAGE),
        HOUR_IMAGE("聊天图片(.*?)时",TimeUnitEnum.HOUR,MessageType.IMAGE),
        DAY_TXT("聊天文字(.*?)天",TimeUnitEnum.DAY,MessageType.TXT),
        HOUR_TXT("聊天文字(.*?)时",TimeUnitEnum.HOUR,MessageType.TXT);

        private String value;
        private TimeUnitEnum timeUnit;
        private MessageType messageType;
        Regex(String value,TimeUnitEnum timeUnit, MessageType messageType){
            this.value = value;
            this.timeUnit = timeUnit;
            this.messageType = messageType;
        }
    }

    public enum MessageType{
        ALL,IMAGE,TXT
    }
}
