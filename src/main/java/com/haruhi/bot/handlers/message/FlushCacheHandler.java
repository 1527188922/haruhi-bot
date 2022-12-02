package com.haruhi.bot.handlers.message;

import com.haruhi.bot.cache.CacheSet;
import com.haruhi.bot.config.BotConfig;
import com.haruhi.bot.constant.GocqActionEnum;
import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.SystemService;
import com.haruhi.bot.ws.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FlushCacheHandler implements IMessageEvent {

    private static final int period = 15;
    private static CacheSet<String> LOCK = new CacheSet<>(period,TimeUnit.SECONDS,1);

    @Autowired
    private SystemService systemService;
    @Override
    public int weight() {
        return 79;
    }

    @Override
    public String funName() {
        return "刷新缓存";
    }

    @Override
    public boolean onMessage(final Message message, final String command) {
        if(!command.matches(RegexEnum.FLUSH_CACHE.getValue())){
            return false;
        }
        if(!message.getUserId().equals(BotConfig.SUPER_USER)){
            log.info("非bot管理员使用了`{}`命令,管理员qq:{}",RegexEnum.FLUSH_CACHE.getValue(),BotConfig.SUPER_USER);
            return true;
        }
        synchronized (LOCK){
            if(LOCK.contains(FlushCacheHandler.class.getSimpleName())){
                Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(), MessageFormat.format("请勿频繁刷新,每次刷新需间隔{0}秒",period), GocqActionEnum.SEND_MSG,true);
                return true;
            }
            ThreadPoolFactory.getCommandHandlerThreadPool().execute(()->{
                systemService.loadCache();
                Client.sendMessage(message.getUserId(),message.getGroupId(),message.getMessageType(),"刷新完成", GocqActionEnum.SEND_MSG,true);
            });
            LOCK.add(FlushCacheHandler.class.getSimpleName());
        }
        return true;
    }


}
