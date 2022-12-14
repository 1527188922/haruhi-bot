package com.haruhi.bot.event.message;

/**
 * 这是一个事件类型的顶级接口
 * 普通消息处理类都实现这个接口
 */
public interface IMessageEventType {
    /**
     * 权重
     * 值越大 优先匹配
     * @return
     */
    int weight();

    String funName();
}
