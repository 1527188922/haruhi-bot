package com.haruhi.bot.event.message;

import com.haruhi.bot.dto.gocq.response.Message;

public interface IOnGroupMessageEvent extends IMessageEventType {
    /**
     * 群聊触发
     * @param message
     * @param command
     */
    boolean onGroup(Message message, String command);
}
