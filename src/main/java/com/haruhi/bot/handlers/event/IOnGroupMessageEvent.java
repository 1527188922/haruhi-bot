package com.haruhi.bot.handlers.event;

import com.haruhi.bot.dto.request.Message;

public interface IOnGroupMessageEvent extends IMessageEventType {
    void onGroup(Message message,String command);
}
