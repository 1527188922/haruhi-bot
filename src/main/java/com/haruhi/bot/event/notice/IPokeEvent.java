package com.haruhi.bot.event.notice;

import com.haruhi.bot.dto.gocq.response.Message;

/**
 * 实现这个接口的类,都能收到戳一戳消息
 */
public interface IPokeEvent extends INoticeEventType{

    boolean onPoke(Message message);
}
