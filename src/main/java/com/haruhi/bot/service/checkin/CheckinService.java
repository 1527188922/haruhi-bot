package com.haruhi.bot.service.checkin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.Params;
import com.haruhi.bot.entity.Checkin;

public interface CheckinService extends IService<Checkin> {

    /**
     * 签到业务
     * @param params
     */
    void checkin(Params params, Message message);

    /**
     * 查看好感业务
     * @param params
     */
    void seeFavorability(Params params, Message message);
}
