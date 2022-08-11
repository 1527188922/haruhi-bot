package com.haruhi.bot.dispenser;

import com.haruhi.bot.constant.event.NoticeEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.event.notice.INoticeEventType;
import com.haruhi.bot.event.notice.IPokeEvent;
import com.haruhi.bot.utils.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * qq通知分发器
 * 通知将通过这个类分发给所有实现了接口 INoticeEventType 的类
 */
@Slf4j
@Component
public class NoticeDispenser {

    private static Map<String, IPokeEvent> pokeEventMap;
    @Autowired
    public void setMessageEventTypeMap(Map<String, IPokeEvent> pokeEventMap){
        NoticeDispenser.pokeEventMap = pokeEventMap;
    }
    private static Set<INoticeEventType> container = new HashSet<>();

    @Autowired
    private ApplicationContextProvider applicationContextProvider;
    @PostConstruct
    private void loadEvent(){
        log.info("加载消息处理类...");
        for (IPokeEvent value : pokeEventMap.values()) {
            NoticeDispenser.attach(value);
        }
        log.info("加载了{}个通知处理类",container.size());

    }
    public static void attach(INoticeEventType event){
        container.add(event);
    }

    public static void onEvent(final Message message){
        String subType = message.getSub_type();
        if(NoticeEventEnum.poke.toString().equals(subType)){
            for (IPokeEvent value : pokeEventMap.values()) {
                if(value.onPoke(message)){
                    break;
                }
            }
        }
    }
}
