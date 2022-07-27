package com.haruhi.bot.handlers.event;

import com.haruhi.bot.dto.request.Message;

public interface IOnPrivateMessageEvent extends IMessageEventType {
    void onPrivate(Message message,String command);
}
