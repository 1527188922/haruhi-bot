package com.haruhi.bot.service.wordStrip;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haruhi.bot.entity.WordStrip;
import com.haruhi.bot.handlers.message.wordStrip.WordStripHandler;
import com.haruhi.bot.mapper.WordStripMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class WordStripServiceImpl extends ServiceImpl<WordStripMapper, WordStrip> implements WordStripService{

    @Autowired
    private WordStripMapper wordStripMapper;

    /**
     * 将数据库词条加载到缓存
     */
    @Override
    public void loadWordStrip(){
        List<WordStrip> wordStrips = wordStripMapper.selectList(null);
        if (!CollectionUtils.isEmpty(wordStrips)){
            for (WordStrip element : wordStrips) {
                WordStripHandler.cache.put(element.getGroupId() + "-" + element.getKeyWord(),element.getAnswer());
            }
            log.info("加载词条数据到内存完成，数量：{}",wordStrips.size());
        }
    }
}
