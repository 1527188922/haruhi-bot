package com.haruhi.bot.thread;

import com.haruhi.bot.service.function.DisableFunctionService;
import com.haruhi.bot.service.pokeReply.PokeReplyService;
import com.haruhi.bot.service.verbalTricks.VerbalTricksService;
import com.haruhi.bot.service.wordStrip.WordStripService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 该线程用来执行bot刚启动时就需要执行的一些有关业务的处理
 * 1：将词条加载到缓存 避免每收到一条消息都去查库
 * 2：加载话术
 */
@Slf4j
@Component
public class FirstTask implements Runnable{

    @Autowired
    private WordStripService wordStripService;
    @Autowired
    private VerbalTricksService verbalTricksService;
    @Autowired
    private PokeReplyService pokeReplyService;
    @Autowired
    private DisableFunctionService disableFunctionService;

    @Override
    public void run() {
        // 加载词条到内存
        wordStripService.loadWordStrip();
        // 加载话术到内存
        verbalTricksService.loadVerbalTricks();
        // 加载戳一戳回复
        pokeReplyService.loadPokeReply();
        // 加载被禁用的功能
        disableFunctionService.loadBanFunction();
    }

    public void execute(FirstTask self){
        new Thread(self).start();
    }
}
