package com.haruhi.bot.event.message;

import com.haruhi.bot.dto.gocq.request.Message;

public interface IOnPrivateMessageEvent extends IMessageEventType {
    /**
     * 私聊触发
     * @param message
     * @param command
     */
    void onPrivate(Message message, String command);
}
