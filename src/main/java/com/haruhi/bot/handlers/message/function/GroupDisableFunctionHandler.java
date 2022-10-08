package com.haruhi.bot.handlers.message.function;

import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.DisableFunction;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.mapper.DisableFunctionMapper;
import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Date;

@Slf4j
@Component
public class GroupDisableFunctionHandler implements IGroupMessageEvent {

    @Autowired
    private DisableFunctionMapper disableFunctionMapper;

    @Override
    public int weight() {
        return 107;
    }

    @Override
    public String funName() {
        return "禁用群功能";
    }

    @Override
    public boolean onGroup(final Message message,final String command) {
        if(!message.getUser_id().equals(BotConfig.SUPER_USER)){
            return false;
        }
        String fun = CommonUtil.commandReplaceFirst(command, RegexEnum.GROUP_DISABLE_FUNCTION);
        if (Strings.isBlank(fun)){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new Task(message,fun,disableFunctionMapper));
        return true;
    }

    private class Task implements Runnable{
        private Message message;
        private String fun;
        private DisableFunctionMapper mapper;
        public Task(Message message,String fun,DisableFunctionMapper mapper){
            this.message = message;
            this.fun = fun;
            this.mapper = mapper;
        }
        @Override
        public void run() {
            try {
                IMessageEventType messageEventType = MessageDispenser.findHandler(fun);
                if (messageEventType == null) {
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),MessageEventEnum.group, MessageFormat.format("没有功能:[{0}]",fun), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                Class<? extends IMessageEventType> aClass = messageEventType.getClass();
                if (MessageDispenser.isBanFunctionByGroup(aClass, message.getGroup_id())) {
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),MessageEventEnum.group, MessageFormat.format("群功能:[{0}]已经被禁用",fun), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                MessageDispenser.setGroupBanFunction(message.getGroup_id(),aClass);
                DisableFunction param = new DisableFunction();
                param.setGlobal(false);
                param.setDisableTime(new Date());
                param.setName(messageEventType.funName());
                param.setWeight(messageEventType.weight());
                param.setClassName(aClass.getName());
                param.setGroupId(message.getGroup_id());
                mapper.insert(param);
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),MessageEventEnum.group,MessageFormat.format("群禁用[{0}]成功",fun), GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(), MessageEventEnum.group, MessageFormat.format("禁用群功能[{0}]时发生异常:{1}",fun,e.getMessage()), GocqActionEnum.SEND_MSG,true);
                log.error("禁用群功能时发生异常",e);
            }
        }
    }
}
