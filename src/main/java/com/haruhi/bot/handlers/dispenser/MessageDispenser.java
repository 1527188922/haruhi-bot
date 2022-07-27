package com.haruhi.bot.handlers.dispenser;

import com.alibaba.fastjson.JSONObject;
import com.haruhi.bot.constant.MessageTypeEnum;
import com.haruhi.bot.dto.request.Message;
import com.haruhi.bot.handlers.event.IMessageEventType;
import com.haruhi.bot.handlers.event.IOnGroupMessageEvent;
import com.haruhi.bot.handlers.event.IOnMessageEvent;
import com.haruhi.bot.handlers.event.IOnPrivateMessageEvent;
import com.haruhi.bot.utils.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class MessageDispenser {

    private static Map<String, IMessageEventType> messageEventTypeMap;
    @Autowired
    public void setMessageEventTypeMap(Map<String, IMessageEventType> messageEventTypeMap){
        MessageDispenser.messageEventTypeMap = messageEventTypeMap;
    }
    private static Set<IMessageEventType> container;
    /**
     * 所有私聊事件处理类
     */
    private static Set<IOnPrivateMessageEvent> privateMessageEventSet;
    /**
     * 所有群聊天事件处理类
     */
    private static Set<IOnGroupMessageEvent> groupMessageEventSet;
    /**
     * 群聊和私聊事件处理
     */
    private static Set<IOnMessageEvent> messageEventSet;

    static {
        container = new HashSet<>();
        privateMessageEventSet = new HashSet<>();
        groupMessageEventSet = new HashSet<>();
        messageEventSet = new HashSet<>();
    }

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
            if(value instanceof IOnPrivateMessageEvent){
                privateMessageEventSet.add((IOnPrivateMessageEvent)value);
            }
            if(value instanceof IOnGroupMessageEvent){
                groupMessageEventSet.add((IOnGroupMessageEvent)value);
            }
            if(value instanceof IOnMessageEvent){
                messageEventSet.add((IOnMessageEvent)value);
            }
        }
        log.info("加载了{}个消息处理类",container.size());

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

    public static void onEvent(String json, String command){
        Message message = JSONObject.parseObject(json, Message.class);
        String messageType = message.getMessage_type();

        for (IOnMessageEvent element : MessageDispenser.messageEventSet) {
            if(container.contains(element)){
                element.onMessage(message,command);
            }
        }
        if(MessageTypeEnum.group.getType().equals(messageType)){
            for (IOnGroupMessageEvent element : MessageDispenser.groupMessageEventSet) {
                if(container.contains(element)){
                    element.onGroup(message,command);
                }
            }
        } else if(MessageTypeEnum.privat.getType().equals(messageType)){
            for (IOnPrivateMessageEvent element : MessageDispenser.privateMessageEventSet) {
                if(container.contains(element)){
                    element.onPrivate(message,command);
                }
            }
        }
    }
}
