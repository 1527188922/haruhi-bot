package com.haruhi.bot.service.pixiv;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.entity.Pixiv;
import com.haruhi.bot.mapper.PixivMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PixivServiceImpl extends ServiceImpl<PixivMapper, Pixiv> implements PixivService{

    @Autowired
    private PixivMapper pixivMapper;

    @Override
    public Pixiv roundOneByTag(boolean isR18, String tag) {
        return pixivMapper.roundOneByTag(isR18,tag);
    }
}
