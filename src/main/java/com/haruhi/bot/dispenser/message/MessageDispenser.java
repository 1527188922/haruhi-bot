package com.haruhi.bot.dispenser.message;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.event.message.IOnGroupMessageEvent;
import com.haruhi.bot.event.message.IOnMessageEvent;
import com.haruhi.bot.event.message.IOnPrivateMessageEvent;
import com.haruhi.bot.utils.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MessageDispenser {

    private static Map<String, IMessageEventType> messageEventTypeMap;
    @Autowired
    public void setMessageEventTypeMap(Map<String, IMessageEventType> messageEventTypeMap){
        MessageDispenser.messageEventTypeMap = messageEventTypeMap;
    }
    private static List<IMessageEventType> container = new ArrayList<>();

    /**
     * 虽没被引用
     * 但不可删除
     */
    @Autowired
    private ApplicationContextProvider applicationContextProvider;
    @PostConstruct
    private void loadEvent(){
        log.info("加载消息处理类...");
        for (IMessageEventType value : messageEventTypeMap.values()) {
            MessageDispenser.attach(value);
        }
        int size = sortByWeight();
        log.info("加载了{}个消息处理类",size);

    }

    /**
     * 根据权重排序
     * @return
     */
    private int sortByWeight(){
        int size = container.size();
        for (int i = 0; i < size - 1; i++) {
            boolean flag = false;
            for (int j = 0; j < size - i - 1; j++) {
                if(container.get(j).weight() < container.get(j + 1).weight()){
                    IMessageEventType iMessageEventType = container.get(j);
                    container.set(j,container.get(j + 1));
                    container.set(j + 1,iMessageEventType);
                    flag = true;
                }
            }
            if(!flag){
                break;
            }
        }
        return size;
    }

    public static void attach(IMessageEventType event){
        container.add(event);
    }

    /**
     * 用于从容器中删除消息处理类
     * 可以实现禁用某命令/功能
     * 这里不能持久化
     * @param clazz
     * @param <T>
     */
    public static <T> void detach(Class<T> clazz){
        T bean = ApplicationContextProvider.getBean(clazz);
        if(bean instanceof IMessageEventType){
            container.remove(bean);
        }
    }

    public static void onEvent(final Message message,final String command){
        String messageType = message.getMessage_type();
        AtomicInteger total = new AtomicInteger(0);
        for (IMessageEventType element : container) {
            if(element instanceof IOnMessageEvent){
                IOnMessageEvent event = (IOnMessageEvent) element;
                if(event.matches(message,command,total)){
                    total.incrementAndGet();
                    event.onMessage(message,command);
                }
            }
            if(MessageTypeEnum.group.getType().equals(messageType)){
                if(element instanceof IOnGroupMessageEvent){
                    IOnGroupMessageEvent event = (IOnGroupMessageEvent) element;
                    if(event.matches(message,command,total)){
                        total.incrementAndGet();
                        event.onGroup(message,command);
                    }
                }
            }else if(MessageTypeEnum.privat.getType().equals(messageType)){
                if(element instanceof IOnPrivateMessageEvent){
                    IOnPrivateMessageEvent event = (IOnPrivateMessageEvent) element;
                    if(event.matches(message,command,total)){
                        total.incrementAndGet();
                        event.onPrivate(message,command);
                    }

                }
            }
        }
    }
}
