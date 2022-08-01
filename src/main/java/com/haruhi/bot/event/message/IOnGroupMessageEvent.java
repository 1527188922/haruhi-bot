package com.haruhi.bot.event.message;

import com.haruhi.bot.dto.gocq.request.Message;

public interface IOnGroupMessageEvent extends IMessageEventType {
    /**
     * 群聊触发
     * @param message
     * @param command
     */
    void onGroup(Message message, String command);
}
