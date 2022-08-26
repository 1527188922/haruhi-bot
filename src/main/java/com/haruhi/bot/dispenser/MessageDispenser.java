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
     * 降序
     * @return
     */
    private int sortByWeight(){
        checkWeight();
        container = container.stream().sorted(Comparator.comparing(IMessageEventType::weight).reversed()).collect(Collectors.toList());
        return container.size();
    }

    private static <T extends IMessageEventType> void attach(T event){
        container.add(event);
    }

    /**
     * 对外提供的添加处理类的方法
     * 添加对象必须是IOC容器中存在的对象
     * @param clazz 类模板对象 同一个类的模板对象必为唯一
     * @param <T>
     */
    public static <T extends IMessageEventType> void attach(Class<T> clazz){
        T bean = ApplicationContextProvider.getBean(clazz);
        container.add(bean);
    }

    /**
     * 用于从容器中删除消息处理类
     * 可以实现禁用某命令/功能
     * 删除对象必须是IOC容器中存在的对象
     * @param clazz
     * @param <T>
     */
    public static <T extends IMessageEventType> void detach(Class<T> clazz){
        T bean = ApplicationContextProvider.getBean(clazz);
        container.remove(bean);
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

    /**
     * 查找处理类
     * @param fun 可以是name也可以是id(weight)
     * @return
     */
    public static IMessageEventType findHandler(String fun){
        Integer funId;
        IMessageEventType messageEventType;
        try {
            funId = Integer.valueOf(fun);
            messageEventType = findHandlerByWeight(funId.intValue());
        }catch (Exception e){
            messageEventType = findHandlerByName(fun);
        }
        return messageEventType;
    }
    private static IMessageEventType findHandlerByName(String funName){
        for (Map.Entry<String, IMessageEventType> eventTypeEntry : messageEventTypeMap.entrySet()) {
            if(eventTypeEntry.getValue().funName().equals(funName)){
                return eventTypeEntry.getValue();
            }
        }
        return null;
    }
    private static IMessageEventType findHandlerByWeight(int weight){
        for (Map.Entry<String, IMessageEventType> eventTypeEntry : messageEventTypeMap.entrySet()) {
            if(eventTypeEntry.getValue().weight() == weight){
                return eventTypeEntry.getValue();
            }
        }
        return null;
    }

    /**
     * 判断当前功能是否存在
     * (功能是否禁用)
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T extends IMessageEventType> boolean exist(Class<T> tClass){
        T bean = ApplicationContextProvider.getBean(tClass);
        return container.contains(bean);
    }
}
