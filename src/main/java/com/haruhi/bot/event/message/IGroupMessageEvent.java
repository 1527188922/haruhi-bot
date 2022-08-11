package com.haruhi.bot.event.message;

import com.haruhi.bot.dto.gocq.response.Message;

/**
 * 实现这接口的类
 * 都能收到群消息
 */
public interface IGroupMessageEvent extends IMessageEventType {
    /**
     * 群聊触发
     * @param message
     * @param command
     */
    boolean onGroup(Message message, String command);
}
