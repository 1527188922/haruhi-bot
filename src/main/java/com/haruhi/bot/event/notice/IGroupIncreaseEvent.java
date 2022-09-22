package com.haruhi.bot.event.notice;

import com.haruhi.bot.dto.gocq.response.Message;

/**
 * 群加入成员事件
 */
public interface IGroupIncreaseEvent extends INoticeEventType{

    /**
     * user_id : 进群人的qq
     * self_id : 机器人qq
     * group_id : 群号
     * time : 时间 (秒级时间戳)
     * @param message
     */
    void onGroupIncrease(Message message);
}
