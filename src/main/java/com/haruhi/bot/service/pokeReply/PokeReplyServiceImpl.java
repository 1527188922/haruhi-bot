package com.haruhi.bot.service.pokeReply;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.entity.PokeReply;
import com.haruhi.bot.handlers.notice.PokeHandler;
import com.haruhi.bot.mapper.PokeReplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PokeReplyServiceImpl extends ServiceImpl<PokeReplyMapper, PokeReply> implements PokeReplyService{

    @Autowired
    private PokeReplyMapper pokeReplyMapper;

    @Override
    public void loadPokeReply() {
        List<PokeReply> list = pokeReplyMapper.selectList(null);
        if(list != null && list.size() > 0){
            for (PokeReply pokeReply : list) {
                PokeHandler.cache.add(pokeReply.getReply());
            }
            // 如果为空字符串 则发送戳一戳
            for (int i = 0; i < 4; i++) {
                PokeHandler.cache.add("");
            }
        }
    }
}
