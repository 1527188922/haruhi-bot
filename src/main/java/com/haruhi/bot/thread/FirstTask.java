package com.haruhi.bot.thread;

import com.haruhi.bot.service.wordStrip.WordStripService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 该线程用来执行bot刚启动时就需要执行的一些有关业务的处理
 * 1：将词条加载到缓存 避免每收到一条消息都去查库
 */
@Slf4j
@Component
public class FirstTask implements Runnable{

    @Autowired
    private WordStripService wordStripService;

    @Override
    public void run() {
        wordStripService.loadWordStrip();
    }

    public void execute(FirstTask self){
        new Thread(self).start();
    }
}
