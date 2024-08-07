package com.haruhi.bot.dispenser;

import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.constant.event.NoticeTypeEnum;
import com.haruhi.bot.constant.event.SubTypeEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.notice.IGroupDecreaseEvent;
import com.haruhi.bot.event.notice.IGroupIncreaseEvent;
import com.haruhi.bot.event.notice.INoticeEventType;
import com.haruhi.bot.event.notice.IPokeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * qq通知分发器
 * 通知将通过这个类分发给所有实现了接口 INoticeEventType 的类
 */
@Slf4j
@Component
public class NoticeDispenser {

    private static Map<String, INoticeEventType> noticeEventTypeMap;
    @Autowired
    public void setMessageEventTypeMap(Map<String, INoticeEventType> pokeEventMap){
        NoticeDispenser.noticeEventTypeMap = pokeEventMap;
    }
    private static List<INoticeEventType> container = new ArrayList<>();

    @PostConstruct
    private void loadEvent(){
        log.info("加载通知处理类...");
        for (INoticeEventType value : noticeEventTypeMap.values()) {
            NoticeDispenser.attach(value);
        }
        log.info("加载了{}个通知处理类",container.size());

    }
    public static void attach(INoticeEventType event){
        container.add(event);
    }

    public static void onEvent(final Message message){
        setMessageType(message);
        String subType = message.getSubType();
        String noticeType = message.getNoticeType();
        log.info("收到通知类消息：subType：{}，noticeType：{}",subType,noticeType);
        if(NoticeTypeEnum.notify.toString().equals(noticeType) && SubTypeEnum.poke.toString().equals(subType)){
            for (INoticeEventType value : container){
                if(value instanceof IPokeEvent){
                    ((IPokeEvent) value).onPoke(message);
                }
            }

        }else if(NoticeTypeEnum.group_increase.toString().equals(noticeType)){
            for (INoticeEventType value : container){
                if(value instanceof IGroupIncreaseEvent){
                    ((IGroupIncreaseEvent) value).onGroupIncrease(message);
                }
            }
        }else if(NoticeTypeEnum.group_decrease.toString().equals(noticeType)){
            for (INoticeEventType value : container){
                if (value instanceof IGroupDecreaseEvent) {
                    ((IGroupDecreaseEvent)value).onGroupDecrease(message);
                }
            }
        }
    }

    private static void setMessageType(final Message message){
        if(Strings.isBlank(message.getMessageType())){
            if(message.getGroupId() != null){
                message.setMessageType(MessageEventEnum.group.getType());
            }else if(message.getUserId() != null){
                message.setMessageType(MessageEventEnum.privat.getType());
            }
        }
    }
}
