package com.haruhi.bot.service.pixiv;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.dto.gocq.request.ForwardMsg;
import com.haruhi.bot.entity.Pixiv;

import java.util.Collection;
import java.util.List;

public interface PixivService extends IService<Pixiv> {

    void roundSend(int num,Boolean isR18,List<String> tags, Message message,String tag);

    void sendPrivate(Collection<Pixiv> pixivs, Message message);
    void sendGroup(Collection<Pixiv> pixivs, List<ForwardMsg> forwardMsgs, Message message);
}
