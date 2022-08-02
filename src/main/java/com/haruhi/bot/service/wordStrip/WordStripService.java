package com.haruhi.bot.service.wordStrip;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haruhi.bot.entity.WordStrip;

public interface WordStripService extends IService<WordStrip> {

    void loadWordStrip();
}
