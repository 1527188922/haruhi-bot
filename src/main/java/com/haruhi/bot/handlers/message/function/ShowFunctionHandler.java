package com.haruhi.bot.handlers.message.function;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.factory.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ShowFunctionHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public String funName() {
        return "显示所有功能";
    }
    @Override
    public boolean onMessage(final Message message,final String command) {
        System.out.println(message.getMessage() == command);
        if (!command.matches(RegexEnum.SHOW_ALL_FUNCTION.getValue())) {
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new ShowFunctionHandler.Task());
        return true;
    }

    public static class Task implements Runnable{

        @Override
        public void run() {
            for (Map.Entry<String, IMessageEventType> eventTypeEntry : MessageDispenser.getMessageEventTypeMap().entrySet()) {

            }
        }
    }
}
