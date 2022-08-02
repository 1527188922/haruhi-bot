package com.haruhi.bot.service.wordStrip;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.entity.WordStrip;
import com.haruhi.bot.handlers.message.wordStrip.WordStripHandler;
import com.haruhi.bot.mapper.WordStripMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordStripServiceImpl extends ServiceImpl<WordStripMapper, WordStrip> implements WordStripService{

    @Autowired
    private WordStripMapper wordStripMapper;

    /**
     * 将数据库词条加载到缓存
     */
    @Override
    public void loadWordStrip(){
        List<WordStrip> wordStrips = wordStripMapper.selectList(null);
        if (wordStrips != null && wordStrips.size() > 0){
            for (WordStrip element : wordStrips) {
                WordStripHandler.cache.put(element.getGroupId() + "-" + element.getKeyWord(),element.getAnswer());
            }
        }
    }
}
