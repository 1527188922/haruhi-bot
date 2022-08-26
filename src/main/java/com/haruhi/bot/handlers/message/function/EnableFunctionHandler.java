package com.haruhi.bot.handlers.message.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dispenser.MessageDispenser;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.entity.DisableFunction;
import com.haruhi.bot.event.message.IMessageEvent;
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

@Slf4j
@Component
public class EnableFunctionHandler implements IMessageEvent {

    @Override
    public int weight() {
        return 106;
    }

    @Override
    public String funName() {
        return "开启功能";
    }

    @Autowired
    private DisableFunctionMapper mapper;

    @Override
    public boolean onMessage(final Message message,final String command) {
        if(!message.getUser_id().equals(BotConfig.SUPER_USER)){
            return false;
        }
        String fun = CommonUtil.commandReplaceFirst(command, RegexEnum.ENABLE_FUNCTION);
        if(Strings.isBlank(fun)){
            return false;
        }

        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new EnableFunctionHandler.Task(message,fun.replace(" ",""),mapper));

        return true;
    }

    public static class Task implements Runnable{
        private Message message;
        private String fun;
        private DisableFunctionMapper mapper;
        Task(Message message,String fun,DisableFunctionMapper mapper){
            this.message = message;
            this.fun = fun;
            this.mapper = mapper;
        }
        @Override
        public void run() {
            try {
                IMessageEventType messageEventType = MessageDispenser.findHandler(fun);
                if (messageEventType == null) {
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("没有功能:[{0}]",fun), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                Class<? extends IMessageEventType> aClass = messageEventType.getClass();
                if(MessageDispenser.exist(aClass)){
                    Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("功能:[{0}]已处于开启中",fun), GocqActionEnum.SEND_MSG,true);
                    return;
                }
                MessageDispenser.attach(aClass);
                LambdaQueryWrapper<DisableFunction> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(DisableFunction::getClassName,aClass.getName()).eq(DisableFunction::getWeight,messageEventType.weight());
                mapper.delete(queryWrapper);
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(),MessageFormat.format("启用[{0}]成功",fun), GocqActionEnum.SEND_MSG,true);
            }catch (Exception e){
                Client.sendMessage(message.getUser_id(),message.getGroup_id(),message.getMessage_type(), MessageFormat.format("开启功能[{0}]时发生异常:{1}",fun,e.getMessage()), GocqActionEnum.SEND_MSG,true);
                log.error("开启功能时发生异常",e);
            }
        }
    }
}
