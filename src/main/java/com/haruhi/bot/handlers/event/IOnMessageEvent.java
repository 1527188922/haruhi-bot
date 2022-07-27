package com.haruhi.bot.handlers.event;

import com.haruhi.bot.dto.request.Message;

public interface IOnMessageEvent extends IMessageEventType {
    void onMessage(Message message,String command);
}
