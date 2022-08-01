package com.haruhi.bot.event.message;

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
     * 重写该方法，参数final修饰，在该方法中，参数严谨修改
     * 禁止修改是指 禁止改变内存指向
     * @return
     */
    boolean matches(Message message, String command, AtomicInteger total);
}
