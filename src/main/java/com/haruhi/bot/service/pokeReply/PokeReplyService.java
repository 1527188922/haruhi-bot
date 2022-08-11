package com.haruhi.bot.service.pokeReply;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.entity.PokeReply;

public interface PokeReplyService extends IService<PokeReply> {

    void loadPokeReply();
}
