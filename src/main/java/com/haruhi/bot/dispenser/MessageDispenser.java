package com.haruhi.bot.dispenser;

import com.haruhi.bot.constant.event.MessageEventEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEventType;
import com.haruhi.bot.event.message.IGroupMessageEvent;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.event.message.IPrivateMessageEvent;
import com.haruhi.bot.utils.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 普通消息分发器
 * 收到群聊 私聊消息时
 * 消息将通过这个类分发给所有实现了接口 IMessageEventType 的类
 */
@Slf4j
@Component
public class MessageDispenser {

    private static Map<String, IMessageEventType> messageEventTypeMap;
    public static Map<String, IMessageEventType> getMessageEventTypeMap(){
        return messageEventTypeMap;
    }
    @Autowired
    public void setMessageEventTypeMap(Map<String, IMessageEventType> messageEventTypeMap){
        MessageDispenser.messageEventTypeMap = messageEventTypeMap;
    }
    private static List<IMessageEventType> container = new ArrayList<>();
    public static List<IMessageEventType> getContainer(){
        return container;
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
        }
        int size = sortByWeight();
        log.info("加载了{}个消息处理类",size);

    }

    private void checkWeight(){
        List<Integer> weights = container.stream().map(IMessageEventType::weight).collect(Collectors.toList());
        Set<Integer> weightSet = new HashSet<>(weights);
        if(weightSet.size() != weights.size()){
            throw new RuntimeException("Duplicate weight appear");
        }
    }

    /**
     * 根据权重排序
     * @return
     */
    private int sortByWeight(){
        checkWeight();
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

        for (IMessageEventType element : container) {
            if(element instanceof IMessageEvent){
                IMessageEvent event = (IMessageEvent) element;
                if(event.onMessage(message,command)){
                    break;
                }
            }
            if(MessageEventEnum.group.getType().equals(messageType)){
                if(element instanceof IGroupMessageEvent){
                    IGroupMessageEvent event = (IGroupMessageEvent) element;
                    if(event.onGroup(message,command)){
                        break;
                    }
                }
            }else if(MessageEventEnum.privat.getType().equals(messageType)){
                if(element instanceof IPrivateMessageEvent){
                    IPrivateMessageEvent event = (IPrivateMessageEvent) element;
                    if(event.onPrivate(message,command)){
                        break;
                    }
                }
            }
        }
    }
}
