package com.haruhi.bot.handlers.message.chatHistory;

import com.haruhi.bot.constant.CqCodeTypeEnum;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.event.message.IOnGroupMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.groupChatHistory.GroupChatHistoryService;
import com.haruhi.bot.ws.Client;
import com.simplerobot.modules.utils.KQCodeUtils;
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
public class FindChatMessageHandler implements IOnGroupMessageEvent {
    @Override
    public int weight() {
        return 91;
    }

    @Autowired
    private GroupChatHistoryService groupChatHistoryService;

    @Override
    public boolean onGroup(final Message message,final String command) {
        Param param = matching(message,command);
        if(param == null){
            return false;
        }
        KQCodeUtils instance = KQCodeUtils.getInstance();
        String[] cqs = instance.getCqs(command, CqCodeTypeEnum.at.getType());

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new FindChatMessageHandler.Task(groupChatHistoryService,message,param,cqs));
        return true;
    }

    public static class Task implements Runnable{
        private GroupChatHistoryService service;
        private Message message;
        private FindChatMessageHandler.Param param;
        private String[] cqs;

        public Task(GroupChatHistoryService service,Message message,FindChatMessageHandler.Param param,String[] cqs){
            this.service = service;
            this.message = message;
            this.param = param;
            this.cqs = cqs;
        }
        @Override
        public void run() {
            service.sendChatList(message,param,cqs);
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
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageTypeEnum.group, MessageFormat.format("错误的参数[{0}],请输入整数...",args),GocqActionEnum.SEND_MSG,true);
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
        private TimeUnit unit;
        private MessageType messageType;
    }

    public enum Regex{

        DAY_ALL("聊天记录(.*?)天",TimeUnit.DAY,MessageType.ALL),
        HOUR_ALL("聊天记录(.*?)时",TimeUnit.HOUR,MessageType.ALL),
        DAY_IMAGE("聊天图片(.*?)天",TimeUnit.DAY,MessageType.IMAGE),
        HOUR_IMAGE("聊天图片(.*?)时",TimeUnit.HOUR,MessageType.IMAGE);

        private String value;
        private TimeUnit timeUnit;
        private MessageType messageType;
        Regex(String value,TimeUnit timeUnit, MessageType messageType){
            this.value = value;
            this.timeUnit = timeUnit;
            this.messageType = messageType;
        }
    }

    public enum TimeUnit{
        DAY,HOUR
    }
    public enum MessageType{
        ALL,IMAGE
    }
}
