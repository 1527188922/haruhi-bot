package com.haruhi.bot.handlers.message.pixiv;

import com.haruhi.bot.constant.RegexEnum;
import com.haruhi.bot.dto.gocq.response.Message;
import com.haruhi.bot.factory.ThreadPoolFactory;
import com.haruhi.bot.event.message.IMessageEvent;
import com.haruhi.bot.service.pixiv.PixivService;
import com.haruhi.bot.utils.CommonUtil;
import com.simplerobot.modules.utils.KQCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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
        ThreadPoolFactory.getCommandHandlerThreadPool().execute(new PixivTask(pixivService,tag,message));
        return true;
    }
    private void after(){
        this.tag = null;
    }

    private class PixivTask implements Runnable{
        private PixivService pixivService;
        private String tag;
        private Message message;
        public PixivTask(PixivService pixivService, String tag, Message message){
            this.tag = tag;
            this.pixivService = pixivService;
            this.message = message;
        }

        @Override
        public void run() {
            pixivService.roundSend(20,null,tag,message);
        }

    }

}
