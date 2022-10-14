package com.haruhi.bot.handlers.message.pixiv;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.service.pixiv.PixivService;
import com.simplerobot.modules.utils.KQCodeUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Component
public class PixivHandler implements IMessageEvent {
    @Override
    public int weight() {
        return 97;
    }

    @Override
    public String funName() {
        return "pix根据tag";
    }

    @Autowired
    private PixivService pixivService;

    private List<String> tags;
    private String tag;

    public boolean matching(final String command) {
        KQCodeUtils instance = KQCodeUtils.getInstance();
        String cq = instance.getCq(command, 0);
        if(cq != null){
            after();
            return false;
        }
        String[] split = RegexEnum.PIXIV.getValue().split("\\|");
        for (String s : split) {
            if (command.startsWith(s)) {
                tag = command.replace(s,"");
                if(Strings.isBlank(tag)){
                    tags = new ArrayList<>(1);
                }else{
                    tags = Arrays.asList(tag.split(",|，"));
                }
                return true;
            }
        }
        after();
        return false;
    }

    @Override
    public boolean onMessage(final Message message,final String command) {
        if (!matching(command)){
            return false;
        }
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivTask(pixivService,tags,message,tag));
        return true;
    }
    private void after(){
        this.tags = null;
        this.tag = null;
    }

    private class PixivTask implements Runnable{
        private PixivService pixivService;
        private String tag;
        private List<String> tags;
        private Message message;
        public PixivTask(PixivService pixivService, List<String> tags, Message message,String tag){
            this.tags = tags;
            this.tag = tag;
            this.pixivService = pixivService;
            this.message = message;
        }

        @Override
        public void run() {
            pixivService.roundSend(40,null,tags,tag,message);
        }

    }

}
