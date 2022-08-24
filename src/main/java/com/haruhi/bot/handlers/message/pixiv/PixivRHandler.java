package com.haruhi.bot.handlers.message.pixiv;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.pixiv.PixivService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PixivRHandler implements IMessageEvent {

    @Autowired
    private PixivService pixivService;

    @Override
    public int weight() {
        return 102;
    }

    @Override
    public String funName() {
        return "pixr根据tag";
    }

    @Override
    public boolean onMessage(final Message message,final String command) {
        String[] split = RegexEnum.PIXIV_R.getValue().split("\\|");
        boolean flag = false;
        String tag = null;
        for (String s : split) {
            if (command.startsWith(s)) {
                flag = true;
                tag = command.replaceFirst(s,"");
                break;
            }
        }
        if(!flag){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivRHandler.PixivRTask(message,tag,pixivService));

        return true;
    }
    public static class PixivRTask implements Runnable{
        private Message message;
        private String tag;
        private PixivService pixivService;

        PixivRTask(Message message,String tag,PixivService pixivService){
            this.tag = tag;
            this.message = message;
            this.pixivService = pixivService;
        }

        @Override
        public void run() {
            pixivService.roundSend(20,true,tag,message);
        }
    }
}
