package com.haruhi.bot.handlers.event;

import com.haruhi.bot.dto.gocq.request.Message;

import java.util.concurrent.atomic.AtomicInteger;

public interface IMessageEventType {
    /**
     * 权重
     * 值越大 优先匹配
     * @return
     */
    int weight();
    /**
     * 匹配规则
     * true:表示通过 将执行后续的处理逻辑
     * false 反之...
     * 当这个方法返回true之后,total才会+=1
     * @return
     */
    boolean matches(Message message, String command, AtomicInteger total);
}
