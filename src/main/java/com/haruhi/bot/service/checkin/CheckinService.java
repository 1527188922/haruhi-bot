package com.haruhi.bot.service.checkin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.response.Answer;
import com.haruhi.bot.entity.Checkin;

public interface CheckinService extends IService<Checkin> {

    void checkin(Answer answer);
}
