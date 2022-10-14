package com.haruhi.bot.handlers.message.pixiv;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.service.pixiv.PixivService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> tags = null;
        for (String s : split) {
            if (command.startsWith(s)) {
                flag = true;
                tag = command.replaceFirst(s,"");
                if(Strings.isBlank(tag)){
                    tags = new ArrayList<>(1);
                }else{
                    tags = Arrays.asList(tag.split(",|，"));
                }
                break;
            }
        }
        if(!flag){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivRTask(message,tags,tag,pixivService));

        return true;
    }
    private class PixivRTask implements Runnable{
        private Message message;
        private List<String> tags;
        private String tag;
        private PixivService pixivService;

        PixivRTask(Message message,List<String> tags,String tag,PixivService pixivService){
            this.tag = tag;
            this.tags = tags;
            this.message = message;
            this.pixivService = pixivService;
        }

        @Override
        public void run() {
            pixivService.roundSend(40,true,tags,tag,message);
        }
    }
}
