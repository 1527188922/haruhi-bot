package com.haruhi.bot.handlers.message.function;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ShowFunctionHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 104;
    }

    @Override
    public String funName() {
        return "显示所有功能";
    }
    @Override
    public boolean onMessage(final Message message,final String command) {
        if (!command.matches(RegexEnum.SHOW_ALL_FUNCTION.getValue())) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new ShowFunctionHandler.Task(message));
        return true;
    }

    public static class Task implements Runnable{
        private Message message;
        Task(Message message){
            this.message = message;
        }
        @Override
        public void run() {
            Collection<IMessageEventType> values = MessageDispenser.getMessageEventTypeMap().values().stream().sorted(Comparator.comparing(IMessageEventType::weight)).collect(Collectors.toList());
            StringBuilder stringBuilder = new StringBuilder("所有功能：\n");
            for (IMessageEventType eventType : values) {
                stringBuilder.append("id：").append(eventType.weight()).append("\n");
                stringBuilder.append("名称：").append(eventType.funName()).append("\n");
                if (isDisable(eventType)) {
                    stringBuilder.append("禁用中\n");
                }
                stringBuilder.append("\n");
            }
            stringBuilder.append("*可通过命令`禁用id`或`禁用名称`来禁用功能");
            send(stringBuilder.toString(),message);
        }
    }
    private static void send(String msg,Message message){
        if (MessageEventEnum.privat.getType().equals(message.getMessage_type())) {
            Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getSub_type(),msg, GocqActionEnum.SEND_MSG,true);
        }else if(MessageEventEnum.group.getType().equals(message.getMessage_type())){
            ArrayList<ForwardMsg> param = new ArrayList<>(1);
            param.add(CommonUtil.createForwardMsgItem(msg,message.getSelf_id(), BotConfig.NAME));
            Client.sendMessage(GocqActionEnum.SEND_GROUP_FORWARD_MSG,message.getGroup_id(),param);
        }
    }
    private static boolean isDisable(IMessageEventType eventType){
        return !MessageDispenser.getContainer().contains(eventType);
    }
}