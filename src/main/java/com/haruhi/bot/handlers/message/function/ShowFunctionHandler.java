package com.haruhi.bot.handlers.message.function;

import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
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
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new Task(message));
        return true;
    }

    private class Task implements Runnable{
        private Message message;
        Task(Message message){
            this.message = message;
        }
        @Override
        public void run() {
            Collection<IMessageEventType> values = MessageDispenser.getMessageEventTypeMap().values().stream().sorted(Comparator.comparing(IMessageEventType::weight)).collect(Collectors.toList());
            StringBuilder stringBuilder = new StringBuilder("所有功能：\n");
            for (IMessageEventType eventType : values) {
                stringBuilder.append("id：").append(eventType.weight());
                if (!MessageDispenser.exist(eventType.getClass())) {
                    stringBuilder.append("(全局禁用中)");
                }
                if( MessageEventEnum.group.getType().equals(message.getMessageType())){
                    if(MessageDispenser.isBanFunctionByGroup(eventType.getClass(),message.getGroupId())){
                        stringBuilder.append("(该群禁用中)");
                    }
                }
                stringBuilder.append("\n");
                stringBuilder.append("名称：").append(eventType.funName()).append("\n");

            }
            stringBuilder.append("可通过`禁用功能id`或`禁用功能名称`来禁用功能\n");
            stringBuilder.append("可通过`开启功能id`或`开启功能名称`来开启功能\n");
            stringBuilder.append("可通过`群禁用功能id`或`群禁用功能名称`来针对某个群禁用功能\n");
            stringBuilder.append("可通过`群开启功能id`或`群开启功能名称`来针对某个群开启功能\n");
            stringBuilder.append("bot功能文档https://blog.csdn.net/cxy152718/article/details/126539271");

            Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),stringBuilder.toString(), GocqActionEnum.SEND_MSG,true);

        }
    }

}
