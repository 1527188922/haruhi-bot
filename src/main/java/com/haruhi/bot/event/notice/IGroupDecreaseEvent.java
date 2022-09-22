package com.haruhi.bot.event.notice;

import com.haruhi.bot.dto.gocq.response.Message;

/**
 * 群退出成员事件
 */
public interface IGroupDecreaseEvent extends INoticeEventType{

    /**
     * user_id : 退群人qq
     * operator_id : 操作人qq
     * sub_type : 退群方式(比如是自己退群还是被管理踢出去)
     * self_id : 机器人qq号
     * group_id : 群号
     * time : 时间 (秒级时间戳)
     * @param message
     */
    void onGroupDecrease(Message message);
}
