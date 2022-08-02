package com.haruhi.bot.event.message;

public interface IMessageEventType {
    /**
     * 权重
     * 值越大 优先匹配
     * @return
     */
    int weight();

}
