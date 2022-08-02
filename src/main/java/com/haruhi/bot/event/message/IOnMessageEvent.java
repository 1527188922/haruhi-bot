package com.haruhi.bot.event.message;

import com.haruhi.bot.dto.gocq.request.Message;

public interface IOnMessageEvent extends IMessageEventType {
    /**
     * 群聊私聊都触发
     * @param message 由go-cqhttp发来的json串 转换过来的java bean
     * @param command 命令 实际上就是对方发来的消息 message 对象中也可以获得
     */
    boolean onMessage(Message message,String command);
}
