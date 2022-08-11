package com.haruhi.bot.event.message;

import com.haruhi.bot.dto.gocq.response.Message;

/**
 * 实现这个接口的类
 * 都能收到私聊消息
 */
public interface IPrivateMessageEvent extends IMessageEventType {
    /**
     * 私聊触发
     * @param message
     * @param command
     */
    boolean onPrivate(Message message, String command);
}
