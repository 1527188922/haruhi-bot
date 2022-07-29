package com.haruhi.bot.service.checkin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.dto.gocq.response.Answer;
import com.haruhi.bot.entity.Checkin;

public interface CheckinService extends IService<Checkin> {

    /**
     * 签到业务
     * @param answer
     */
    void checkin(Answer answer, Message message);

    /**
     * 查看好感业务
     * @param answer
     */
    void seeFavorability(Answer answer, Message message);
}
