package com.haruhi.bot.service.pixiv;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.gocq.request.Message;
import com.haruhi.bot.entity.Pixiv;

public interface PixivService extends IService<Pixiv> {

    void roundSend(int num,Boolean isR18,String tag, Message message);
}
